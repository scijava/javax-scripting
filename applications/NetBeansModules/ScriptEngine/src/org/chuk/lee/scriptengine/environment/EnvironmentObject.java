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
 * EnvironmentObject.java
 *
 * Created on July 3, 2006, 1:23 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.chuk.lee.scriptengine.environment;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.*;
import org.chuk.lee.scriptengine.*;
import org.chuk.lee.scriptengine.ConfigurationValues;
import org.chuk.lee.scriptengine.Utilities;
import org.openide.nodes.*;

import static org.chuk.lee.scriptengine.Utilities.*;

/**
 *
 * @author projects
 */
public class EnvironmentObject extends Children.Keys implements PropertyChangeListener {
    
    /**
     * Creates a new instance of EnvironmentObject
     */
    
    ScriptRepositoryObject[] rootList = new ScriptRepositoryObject[0];
    Object[] children = new Object[0];
    
    public EnvironmentObject() {
        Singletons.getConfigurationValues().addPropertyChangeListener(this);
    }
    
    protected void addNotify() {
        load();
    }
    
    protected void removeNotify() {
        setKeys(Collections.EMPTY_SET);
    }
    
    private void load() {
        
        //TODO need to create the objects here
        String rep = Singletons.getConfigurationValues().getScriptRepository();
        if ((rep != null) && (rep.trim().length() > 0)) {
            //TODO need to scan repository for directories
            File repDir = new File(rep);
            File[] dirList = repDir.listFiles(new FileFilter() {
                public boolean accept(File f) {
                    return ((!f.getName().startsWith(".")) && f.isDirectory());
                }
            });
            children = new Object[dirList.length + 2];
            rootList = new ScriptRepositoryObject[dirList.length];
            for (int i = 0; i < dirList.length; i++) {
                rootList[i] = new ScriptRepositoryObject(rep, dirList[i].getName());
                children[i] = rootList[i];
            }
            children[dirList.length] = new InstalledLanguagesObject();
            children[dirList.length + 1] = new InstalledScriptModuleObject();
        } else
            children = new Object[]{new InstalledLanguagesObject()
            , new InstalledScriptModuleObject()};
        
        setKeys(children);
    }
    
    protected Node[] createNodes(Object obj) {
        
        Node theNode = null;
        if (obj instanceof InstalledLanguagesObject)
            theNode = new InstalledLanguagesObjectNode((InstalledLanguagesObject)obj);
        
        else if (obj instanceof InstalledScriptModuleObject)
            theNode = new InstalledScriptModuleNode((InstalledScriptModuleObject)obj);
        
        else if (obj instanceof ScriptRepositoryObject)
            theNode = new TopLevelScriptNode((ScriptRepositoryObject)obj);
        
        //TODO need to
        return (new Node[]{ theNode });
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        
        if (!evt.getPropertyName().equals(ConfigurationValues.SCRIPT_REPOSITORY))
            return;
        
        reload();
    }
    
    public void reload() {
        Thread t = new Thread(new Runnable() {
            public void run() {
                load();
            }
        });
        t.start();
    }
    
    public void reload(final String root) {
        Thread t = new Thread(new Runnable() {
            public void run() {
                for (int i = 0; i < rootList.length; i++) {
                    File f = rootList[i].getFileObject();
                    if (!root.startsWith(f.getAbsolutePath()))
                        continue;
                    rootList[i] = new ScriptRepositoryObject(f);
                    children[i] = rootList[i];       
                    //This is not optimal
                    setKeys(children);
                }
            }
        });
        t.start();
    }
}
