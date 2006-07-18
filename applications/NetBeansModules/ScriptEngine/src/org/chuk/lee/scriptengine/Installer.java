/*
Copyright � 2006 Sun Microsystems, Inc., 4150 Network Circle, Santa  Clara, California
95054, U.S.A. All rights reserved.U.S. Government Rights - Commercial software.
Government users are subject to the Sun Microsystems, Inc. standard license agreement
and applicable provisions of the FAR and its supplements.  Use is subject to license
terms.  Sun, Sun Microsystems,  the Sun logo,  Java and  Jini are trademarks or registered
trademarks of Sun Microsystems, Inc. in the U.S. and other countries. This product is
covered and controlled by U.S. Export Control laws and may be subject to the export or import
laws in other countries.  Nuclear, missile, chemical biological weapons or nuclear maritime 
end uses or end users, whether direct or indirect, are strictly prohibited. Export or reexport
to countries subject to U.S. embargo or to entities identified on U.S. Export exclusion lists,
including, but not limited to, the denied persons and specially designated nationals lists is
strictly prohibited.

Copyright � 2006 Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,  California 95054,
Etats-Unis. Tous droits r�serv�s.L'utilisation est soumise aux termes de la Licence.Sun, Sun
Microsystems,  le logo Sun,  Java et  Jini sont des  marques de fabrique ou des marques 
d�pos�es de Sun Microsystems, Inc. aux Etats-Unis et dans d'autres pays.Ce produit est soumis
� la l�gislation am�ricaine en mati�re de contr�le des exportations et peut �tre soumis � la 
r�glementation en vigueur dans d'autres pays dans le domaine des exportations et importations.
Les utilisations, ou utilisateurs finaux, pour des armes nucl�aires,des missiles, des armes 
biologiques et chimiques ou du nucl�aire maritime, directement ou indirectement, sont 
strictement interdites. Les exportations ou r�exportations vers les pays sous embargo 
am�ricain, ou vers des entit�s figurant sur les listes d'exclusion d'exportation am�ricaines,
y compris, mais de mani�re non exhaustive, la liste de personnes qui font objet d'un ordre de
ne pas participer, d'une fa�on directe ou indirecte, aux exportations des produits ou des 
services qui sont r�gis par la l�gislation am�ricaine en mati�re de contr�le des exportations
et la liste de ressortissants sp�cifiquement d�sign�s, sont rigoureusement interdites.
*/
package org.chuk.lee.scriptengine;

import java.io.*;
import org.chuk.lee.scriptengine.api.NetBeansIDE;
import org.openide.ErrorManager;
import org.openide.modules.ModuleInstall;

import static org.chuk.lee.scriptengine.Utilities.*;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public class Installer extends ModuleInstall implements Runnable {
    
    public void restored() {
        //Save a copy of the classpath
        ConfigurationValues vals = Singletons.getConfigurationValues();
        vals.setOriginalClassPath(System.getProperty("java.class.path"));
        String ncp = vals.getOriginalClassPath() + File.pathSeparator + vals.getClassPath();
        System.setProperty("java.class.path", ncp);
        try {
            Singletons.setServiceLoader(new ServiceLoaderUtilities(ncp));
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
        
        //Load all script first
        Thread t = new Thread(this);
        t.start();
    }
    
    public boolean closing() {
        //Use async so that the events gets to fire first before the NetBeans closes
        Singletons.getModuleEventSupport().fireEventSync(NetBeansIDE.SHUTDOWN_EVENT);
        return (true);
    }
    
    //Load all startup script
    public void run() {
        ActionScript[] scripts;
        String rep = Singletons.getConfigurationValues().getScriptRepository();
        
        File[] fileList = getScripts(rep + File.separator + "load");
        for (File f: fileList) {
            ActionScript as = new ActionScript(f.getAbsolutePath());
            try {
                //as.initialize(Singletons.getScriptEngineManager());
                as.initialize();
                as.invoke();
            } catch (Exception e) {
                Exception ex = new Exception("Script: " + as.getScriptFile(), e);
                ErrorManager.getDefault().notify(ex);
            }
        }
        //Now we fire the startup events
        Singletons.getModuleEventSupport().fireEvent(NetBeansIDE.STARTUP_EVENT);
    }
}
