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
 * ScriptRepositoryObjectNode.java
 *
 * Created on July 4, 2006, 9:08 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.chuk.lee.scriptengine.environment;

import java.awt.*;
import java.io.*;
import javax.script.ScriptEngine;
import javax.swing.Action;
import org.chuk.lee.scriptengine.Singletons;
import org.openide.nodes.*;

/**
 *
 * @author projects
 */

import static org.chuk.lee.scriptengine.Utilities.*;

public class ScriptRepositoryObjectNode extends AbstractNode {
    
    /**
     * Creates a new instance of ScriptRepositoryObjectNode
     */
    public ScriptRepositoryObjectNode(ScriptRepositoryObject obj) {
        super(obj);
        setDisplayName(obj.getName());
        setSheet(createSheet());
    }
    
    public Action[] getActions(boolean b) {
        
        File f = ((ScriptRepositoryObject)getChildren()).getFileObject();
        
        if (f.isDirectory())
            return (new Action[]{ new CreateScriptAction(f), new CreateDirectoryAction(f) });
        
        else if (f.isFile()) {            
            return (new Action[] { new OpenInEditorAction(f), new ExecuteAction(f), new DeleteAction(f) });
        }
                
        return (null);
    }
    
    public Image getIcon(int i) {
        File f = ((ScriptRepositoryObject)getChildren()).getFileObject();
        ClassLoader loader = this.getClass().getClassLoader();
        if (!f.exists())
            return (loadImage("org/chuk/lee/scriptengine/environment/broken.gif", loader));
        
        else if (f.isDirectory())
            return (loadImage("org/chuk/lee/scriptengine/environment/folder.gif", loader));
        
        else if (f.isFile())
            //TODO should probably check if it is a valid ext
            return (loadImage("org/chuk/lee/scriptengine/script.png", loader));
        
        return (super.getIcon(i));
    }
    
    public Image getOpenedIcon(int i) {
        return (getIcon(i));
    }
    
    protected Sheet createSheet() {
        
        File f = ((ScriptRepositoryObject)getChildren()).getFileObject();
        Sheet sheet = Sheet.createDefault();
        Sheet.Set set = sheet.createPropertiesSet();
        Property prop = null;
        String msg;
        
        if (f.isDirectory()) {
            prop = new ReadOnlyProperty("FILE", f.getAbsolutePath()
            , "Directory", "Directory");
            set.put(prop);
        } else if (f.isFile()) {
            prop = new ReadOnlyProperty("SCRIPT", f.getAbsolutePath()
            , "Script", "Script");
            set.put(prop);
            ScriptEngine eng = Singletons.getScriptEngine(f.getName());
            if (eng == null)
                msg = "JavaScript[Assumed]";
            else
                msg = eng.getFactory().getLanguageName();
            prop = new ReadOnlyProperty("SCRIPT_LANGUAGE", msg
                    , "Script Language", "Script language");
            set.put(prop);
            
        } else
            return (super.createSheet());
        
        sheet.put(set);
        
        return (sheet);
    }
}
