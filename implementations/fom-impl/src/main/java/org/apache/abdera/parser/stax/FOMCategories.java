/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */
package org.apache.abdera.parser.stax;

import java.util.ArrayList;
import java.util.List;

import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Categories;
import org.apache.abdera.model.Category;
import org.apache.abdera.parser.stax.util.FOMHelper;
import org.apache.axiom.fom.AbderaCategories;
import org.apache.axiom.fom.IRIUtil;
import org.apache.axiom.om.OMElement;

public class FOMCategories extends FOMExtensibleElement implements AbderaCategories {
    public Categories addCategory(Category category) {
        addChild((OMElement)category);
        return this;
    }

    public Category addCategory(String term) {
        Category category = getFactory().newCategory(this);
        category.setTerm(term);
        return category;
    }

    public Category addCategory(String scheme, String term, String label) {
        Category category = getFactory().newCategory(this);
        category.setTerm(term);
        category.setScheme(scheme);
        category.setLabel(label);
        return category;

    }

    public List<Category> getCategories() {
        return _getChildrenAsSet(CATEGORY);
    }

    public List<Category> getCategories(String scheme) {
        return FOMHelper.getCategories(this, scheme);
    }

    private List<Category> copyCategoriesWithScheme(List<Category> cats) {
        List<Category> newcats = new ArrayList<Category>();
        IRI scheme = getScheme();
        for (Category cat : cats) {
            Category newcat = (Category)cat.clone();
            if (newcat.getScheme() == null && scheme != null)
                newcat.setScheme(scheme.toString());
            newcats.add(newcat);
        }
        return newcats;
    }

    public List<Category> getCategoriesWithScheme() {
        return copyCategoriesWithScheme(getCategories());
    }

    public List<Category> getCategoriesWithScheme(String scheme) {
        return copyCategoriesWithScheme(getCategories(scheme));
    }

    public IRI getScheme() {
        String value = getAttributeValue(SCHEME);
        return (value != null) ? new IRI(value) : null;
    }

    public boolean isFixed() {
        String value = getAttributeValue(FIXED);
        return (value != null && value.equals(YES));
    }

    public Categories setFixed(boolean fixed) {
        if (fixed && !isFixed())
            setAttributeValue(FIXED, YES);
        else if (!fixed && isFixed())
            removeAttribute(FIXED);
        return this;
    }

    public Categories setScheme(String scheme) {
        if (scheme != null)
            setAttributeValue(SCHEME, new IRI(scheme).toString());
        else
            removeAttribute(SCHEME);
        return this;
    }

    public IRI getHref() {
        return IRIUtil.getUriValue(getAttributeValue(HREF));
    }

    public IRI getResolvedHref() {
        return IRIUtil.resolve(getResolvedBaseUri(), getHref());
    }

    public Categories setHref(String href) {
        if (href != null)
            setAttributeValue(HREF, (new IRI(href)).toString());
        else
            removeAttribute(HREF);
        return this;
    }

    public boolean contains(String term) {
        return contains(term, null);
    }

    public boolean contains(String term, String scheme) {
        List<Category> categories = getCategories();
        IRI catscheme = getScheme();
        IRI uri = (scheme != null) ? new IRI(scheme) : catscheme;
        for (Category category : categories) {
            String t = category.getTerm();
            IRI s = (category.getScheme() != null) ? category.getScheme() : catscheme;
            if (t.equals(term) && ((uri != null) ? uri.equals(s) : s == null))
                return true;
        }
        return false;
    }

    public boolean isOutOfLine() {
        boolean answer = false;
        try {
            answer = getHref() != null;
        } catch (Exception e) {
        }
        return answer;
    }

}
