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
 * CreateDirectoryAction.java
 *
 * Created on July 7, 2006, 11:24 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.chuk.lee.scriptengine.environment;

import java.awt.event.ActionEvent;
import java.io.*;
import javax.swing.AbstractAction;
import org.chuk.lee.scriptengine.*;
import org.openide.*;

/**
 *
 * @author projects
 */
public class CreateDirectoryAction extends AbstractAction {
    
    private File folder;
    
    /** Creates a new instance of CreateDirectoryAction */
    public CreateDirectoryAction(File f) {
        folder = f;
        putValue(NAME, "Create Diectory...");
    }
    
    public void actionPerformed(ActionEvent aEvt) {
        NotifyDescriptor msg;
        NotifyDescriptor.InputLine nd = new NotifyDescriptor.InputLine(
                "Please enter the name of the folder", "Folder name"
                , NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.PLAIN_MESSAGE);
        DialogDisplayer.getDefault().notify(nd);
        if (nd.getValue() != NotifyDescriptor.OK_OPTION)
            return;
        
        String n = nd.getInputText();
        if ((n == null) || (n.trim().length() <= 0)) {
            msg = new NotifyDescriptor.Message("Invalid filename"
                    , NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(msg);
            return;
        }
        
        File fd = new File(folder.getAbsolutePath() + File.separator + nd.getInputText());
        
        try {
            if (fd.mkdir()) {
                Singletons.getEnvironmentObject().reload(folder.getAbsolutePath());
                return;
            }
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
            return;
        }
        
        msg = new NotifyDescriptor.Message("Cannot create " + n, NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(msg);
    }
    
}
