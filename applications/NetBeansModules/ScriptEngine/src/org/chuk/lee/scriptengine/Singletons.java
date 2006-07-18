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
/*
 * Singletons.java
 *
 * Created on June 26, 2006, 5:03 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.chuk.lee.scriptengine;

import java.net.*;
import java.util.HashMap;
import javax.script.*;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;
import org.chuk.lee.scriptengine.api.*;
import org.chuk.lee.scriptengine.environment.EnvironmentObject;

/**
 *
 * @author projects
 */
public class Singletons {
    
    private static ScriptEngineManager manager = null;
    private static HashMap<String, Object> sessionData = null;
    private static String originalClassPath = null;
    private static ModuleEventSupport eventSupport = new ModuleEventSupport();
    private static ServiceLoaderUtilities utils = null;
    private static EnvironmentObject env = null;
    
    public static void setEnvironmentObject(EnvironmentObject e) {
        env = e;
    }
    public static EnvironmentObject getEnvironmentObject() {
        return (env);
    }
    
    public static void setServiceLoader(ServiceLoaderUtilities u) {
        utils = u;
        //Force a reload of the ScriptEngineManager
        manager = null;
    }
    public static ServiceLoaderUtilities getServiceLoader() {
        return (utils);
    }
    
    public static ModuleEventSupport getModuleEventSupport() {
        return (eventSupport);
    }
    
    public static void setOriginalClassPath(String cp) {
        originalClassPath = cp;
    }
    public static String getOriginalClassPath() {
        return (originalClassPath);
    }
    
    public static ScriptEngineManager getScriptEngineManager() {
        if (manager == null) {
            if (utils != null)
                manager = new ScriptEngineManager(utils.getClassLoader());
            else
                manager = new ScriptEngineManager();
        }
        return (manager);
    }
    
    public static ScriptEngine getScriptEngine(String path) {
        
        ScriptEngineManager mgr;
        int i = path.lastIndexOf(".");
        String suffix = "js";
        
        if (i != -1)
            suffix = path.substring(++i);
        
        return (getScriptEngineManager().getEngineByExtension(suffix));
    }
    
    public static HashMap<String, Object> getSessionStorage() {
        if (sessionData == null)
            sessionData = new HashMap<String, Object>();
        return (sessionData);
    }
    
    public static ConfigurationValues getConfigurationValues() {
        return (ConfigurationValues.getDefault());
    }
    
    public static PersistentStorage getPersistentStorage() {
        return (PersistentStorage.getDefault());
    }
}
