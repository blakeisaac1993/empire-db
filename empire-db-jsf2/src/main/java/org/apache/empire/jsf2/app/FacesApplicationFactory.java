/*
 * ESTEAM Software GmbH, 18.04.2012
 */
package org.apache.empire.jsf2.app;

import java.util.Map;

import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.context.FacesContext;

import org.apache.empire.exceptions.InternalException;
import org.apache.empire.exceptions.InvalidArgumentException;
import org.apache.empire.exceptions.ItemExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.faces.application.InjectionApplicationFactory;

public abstract class FacesApplicationFactory extends ApplicationFactory
{
    private static final Logger  log = LoggerFactory.getLogger(FacesApplicationFactory.class);

    private Class<? extends FacesApplication> applicationClass;
    
    private volatile Application application;
    
    protected FacesApplicationFactory(Class<? extends FacesApplication> applicationClass)
    {
        this.applicationClass  = applicationClass;
    }

    @Override
    public Application getApplication()
    {
        if (application == null)
        {   try
            {   // Create Application
                application = applicationClass.newInstance();
            }
            catch (InstantiationException e)
            {
                throw new InternalException(e);
            }
            catch (IllegalAccessException e)
            {
                throw new InternalException(e);
            }
            // InjectionApplicationFactory.setApplicationInstance(application);
            Map<String, Object> appMap = FacesContext.getCurrentInstance().getExternalContext().getApplicationMap();
            appMap.put(InjectionApplicationFactory.class.getName(), application);
            // log
            log.info("Fin2Application Application instance created");
        }
        return application;
    }

    @Override
    public void setApplication(Application application)
    {
        if (this.application != null)
            throw new ItemExistsException(this.application);
        if (!(application instanceof FacesApplication))
            throw new InvalidArgumentException("application", application);
        this.application = application;
    }
}
