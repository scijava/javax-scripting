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
 * ScriptLanguageObjectNode.java
 *
 * Created on July 6, 2006, 11:36 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.chuk.lee.scriptengine.environment;

import java.awt.Image;
import javax.swing.Action;
import org.chuk.lee.scriptengine.Utilities;
import org.openide.ErrorManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Sheet;

/**
 *
 * @author projects
 */
public class ScriptLanguageObjectNode extends AbstractNode {
    
    private ScriptLanguageObject lang;
    
    /** Creates a new instance of ScriptLanguageObjectNode */
    public ScriptLanguageObjectNode(ScriptLanguageObject slo) {
        super(slo);
        setDisplayName(slo.getLanguageName() + "(" + slo.getEngineName() + ")");
        lang = slo;
    }

    public Action[] getActions(boolean b) {
                
        return (new Action[] { new ShowConsoleAction(lang.getEngineFactoryClass()) });
    }    
    
    public Image getIcon(int i) {
        return (Utilities.loadImage("org/chuk/lee/scriptengine/environment/script.gif"
                , this.getClass().getClassLoader()));
    }
    
    public Image getOpenedIcon(int i) {
        return (getIcon(i));
    }
    
    protected Sheet createSheet() {
        
        Sheet sheet = Sheet.createDefault();
        Sheet.Set set = sheet.createPropertiesSet();
        Property prop = null;       
        
        try {
            prop = new ReadOnlyProperty("LANGUAGE_NAME", lang.getLanguageName()
                    , "Script Language", "Script language name");
            set.put(prop);
            prop = new ReadOnlyProperty("ENGINE_NAME", lang.getEngineName()
                    , "Script Engine", "Script engine name");
            set.put(prop);
            prop = new ReadOnlyProperty("SCRIPT_EXTENSIONS", lang.getExtensions()
                    , "Suffix", "File suffix(es) recognized by this engine");
            set.put(prop);
            prop = new ReadOnlyProperty("MIME", lang.getMimeTypes()
                    , "MIME", "MIME type(s) recognized by this engine");
            set.put(prop);
            
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
            super.createSheet();
        }
        
        sheet.put(set);
        
        return (sheet);
    }
}
