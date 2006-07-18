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
package org.chuk.lee.scriptengine.console;

import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.*;
import org.chuk.lee.scriptengine.ConfigurationValues;
import org.chuk.lee.scriptengine.ScriptEngineTableModel;
import org.chuk.lee.scriptengine.Singletons;
import org.chuk.lee.scriptengine.Utilities;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

import static org.chuk.lee.scriptengine.ScriptEngineTableModel.*;

public final class OpenConsoleAction extends CallableSystemAction implements PropertyChangeListener, ActionListener, Runnable  {
    
    private static final String DEFAULT_CONSOLE = "defaultConsole";
    
    private JMenu main = new JMenu("Script Console");
    private JMenuItem console;
    
    public OpenConsoleAction() {
        Singletons.getConfigurationValues().addPropertyChangeListener(this);
        
        main.setIcon(Utilities.loadIcon(iconResource(), this.getClass().getClassLoader()));

        load();
        
    }
    
    public void performAction() {
        // TODO implement action body
    }
    
    public String getName() {
        return NbBundle.getMessage(OpenConsoleAction.class, "CTL_OpenConsoleAction");
    }
    
    protected String iconResource() {
        return "org/chuk/lee/scriptengine/console/console.gif";
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    public JMenuItem getMenuPresenter() {
        
        return (main);
    }
    
    public void actionPerformed(ActionEvent aEvt) {
        String cmd = aEvt.getActionCommand();
        ScriptEngine eng = Singletons.getServiceLoader().loadScriptEngine(cmd);
        JMenuItem mi = (JMenuItem)aEvt.getSource();
        CommandProcessor cp = null;
        
        if (eng == null) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(
                    "Cannot find script engine for "
                    + mi.getText(), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            return;
        }
        
        try {
            String txt = mi.getText().toLowerCase();
            if ((txt.startsWith("ecmascript")) || (txt.startsWith("javascript"))) {
                cp = new JavaScriptCommandProcessor(eng);
            } else
                cp = new CommandProcessorImpl(eng);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
            return;
        }
        ConsolePanel panel = new ConsolePanel(cp, eng);
        panel.display();
    }
    
    public void propertyChange(PropertyChangeEvent evt) {        

        if (!evt.getPropertyName().equals(ConfigurationValues.ENGINE_LIST))
            return;
        
        Thread t = new Thread(this);
        t.start();
    }
    
    public void run() {
        load();
    }
    
    private void load() {
        main.removeAll();
        //main.add(console);
        //main.add(new JSeparator());
        ScriptEngineTableModel model = Singletons.getServiceLoader().getTableModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            String lang = (String)model.getValueAt(i, LANGUAGE);
            String eng = (String)model.getValueAt(i, ENGINE_NAME);
            String cls = (String)model.getValueAt(i, CLASS);
            JMenuItem mi = new JMenuItem(lang + "(" + eng + ")");
            mi.setActionCommand(cls);
            mi.addActionListener(this);
            main.add(mi);
        }
    }
}
