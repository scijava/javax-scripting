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
 * ServiceLoaderUtilities.java
 *
 * Created on June 29, 2006, 4:48 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.chuk.lee.scriptengine;

import java.io.*;
import java.net.*;
import java.net.URLClassLoader;
import java.util.*;
import javax.script.*;

/**
 *
 * @author projects
 */
public class ServiceLoaderUtilities {
    
    private URLClassLoader classLoader = null;
    private ServiceLoader<ScriptEngineFactory> service = null;
    private ScriptEngineTableModel model = null;
    
    /** Creates a new instance of ServiceLoaderUtilities */
    public ServiceLoaderUtilities(URL[] u) {
        classLoader = new URLClassLoader(u);
        service = ServiceLoader.load(ScriptEngineFactory.class, new URLClassLoader(u));
    }
    public ServiceLoaderUtilities(String cp) throws Exception {
        String[] comp = cp.split(File.pathSeparator);
        URL[] urls = new URL[comp.length];
        for (int i = 0; i < urls.length; i++) {
            //Windows is file:///c:\whatever, Unix is file:///whatever
            if ((comp[i] == null) || (comp[i].trim().length() <= 0))
                continue;
            urls[i] = comp[i].startsWith("/")?
                new URL("file:///" + comp[i].substring(1)):
                new URL("file:///" + comp[i]);
        }
        classLoader = new URLClassLoader(urls);
        service = ServiceLoader.load(ScriptEngineFactory.class, new URLClassLoader(urls));
    }
    
    public Iterator<ScriptEngineFactory> getScriptEngines() {
        return (service.iterator());
    }
    
    public ScriptEngine loadScriptEngine(String s) {               
        
        for (ScriptEngineFactory f: service) {   
            if (f.getClass().getName().equals(s))
                return (f.getScriptEngine());
        }
        
        return (null);
    }
    
    public ClassLoader getClassLoader() {
        return (classLoader);
    }
    
    public ScriptEngineTableModel getTableModel() {
        if (model != null)
            return (model);
        
        Iterator<ScriptEngineFactory> iter = service.iterator();
        ScriptEngineTableModel model = new ScriptEngineTableModel();
        while (iter.hasNext()) {
            ScriptEngineFactory fac =iter.next();
            model.add(fac.getLanguageName(), fac.getExtensions(), fac.getEngineName(), fac.getClass().getName());
        }
        
        return (model);
    }    
}
