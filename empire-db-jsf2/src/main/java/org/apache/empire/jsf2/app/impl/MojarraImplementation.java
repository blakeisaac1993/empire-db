/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.empire.jsf2.app.impl;

import java.util.Map;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.apache.empire.exceptions.ItemExistsException;
import org.apache.empire.jsf2.app.FacesApplication;
import org.apache.empire.jsf2.app.FacesImplementation;

import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.application.ApplicationFactoryImpl;
import com.sun.faces.application.InjectionApplicationFactory;
import com.sun.faces.component.CompositeComponentStackManager;
import com.sun.faces.facelets.el.ContextualCompositeValueExpression;
import com.sun.faces.mgbean.BeanManager;
import com.sun.faces.mgbean.ManagedBeanInfo;

public class MojarraImplementation implements FacesImplementation 
{
	private BeanManager bm;

	@Override
	public void initApplication(final FacesApplication application)
	{
		ApplicationFactoryImpl applFactoryImpl = new ApplicationFactoryImpl();
        // set impl
        application.setImplementation(this, applFactoryImpl.getApplication());
        // Application Map 
        Map<String, Object> appMap = FacesContext.getCurrentInstance().getExternalContext().getApplicationMap();
        appMap.put(InjectionApplicationFactory.class.getName(), application);
        // init Bean Manager
		FacesContext fc = FacesContext.getCurrentInstance();
		bm = ApplicationAssociate.getInstance(fc.getExternalContext()).getBeanManager();
	}

	@Override
	public void registerManagedBean(final String beanName,final String beanClass,final String scope) 
	{
		// check
        if (bm.getRegisteredBeans().containsKey(beanName))
        {
            throw new ItemExistsException(beanName);
        }
        // register now
        ManagedBeanInfo mbi = new ManagedBeanInfo(beanName, beanClass, "view", null, null, null, null);
        bm.register(mbi);
	}

	@Override
	public Object getManagedBean(final String beanName, final FacesContext fc)
	{
		// Find Bean
		Object mbean = bm.getBeanFromScope(beanName, fc);
		if (mbean==null)
			mbean= bm.create(beanName, fc);
        return mbean;
	}
	
	@Override
	public UIComponent getValueParentComponent(final ValueExpression ve) 
	{
        if (ve instanceof ContextualCompositeValueExpression)
        {
            FacesContext ctx = FacesContext.getCurrentInstance();
            ContextualCompositeValueExpression ccve = (ContextualCompositeValueExpression)ve;
            CompositeComponentStackManager manager = CompositeComponentStackManager.getManager(ctx);
            UIComponent cc = manager.findCompositeComponentUsingLocation(ctx, ccve.getLocation());
            // set Parent
            return cc;
        }
        return null;
	}

}