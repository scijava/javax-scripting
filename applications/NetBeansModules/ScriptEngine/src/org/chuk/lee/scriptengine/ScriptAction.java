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

import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.io.IOException;
import java.util.*;
import java.util.LinkedList;
import javax.imageio.ImageIO;
import javax.script.*;
import javax.script.ScriptEngineManager;
import javax.swing.*;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

import static org.chuk.lee.scriptengine.Utilities.*;

public final class ScriptAction extends CallableSystemAction implements ActionListener, PropertyChangeListener {
    
    private static final String RELOAD_COMMAND = "RELOAD";
    
    private JMenu main = null;
    private ScriptEngineManager manager = null;
    
    public ScriptAction() {
        ConfigurationValues values = Singletons.getConfigurationValues();
        values.addPropertyChangeListener(this);
    }
    
    public void performAction() {
        // TODO implement action body
    }
    
    public String getName() {
        return NbBundle.getMessage(ScriptAction.class, "CTL_ScriptAction");
    }
    
    protected String iconResource() {
        return "org/chuk/lee/scriptengine/script_dir.png";
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    public JMenuItem getMenuPresenter() {
        
        if (main == null) {
            main = new JMenu("Scripts");
            main.setIcon(Utilities.loadIcon(iconResource(), this.getClass().getClassLoader()));
            createMenus(main);
        }
        
        return (main);
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        
        if (main != null)
            createMenus(main);
    }
    
    public void actionPerformed(ActionEvent aEvt) {
        
        String cmd = aEvt.getActionCommand();
        ActionScript act = null;
        
        if (cmd.equals(RELOAD_COMMAND)) {
            createMenus(main);
            return;
        }
        
        act = new ActionScript(cmd);
        
        try {
            //act.initialize(Singletons.getScriptEngineManager());
            act.initialize();
            act.invoke();
        } catch (Exception e) {
            Exception ex = new Exception("Script: " + act.getScriptFile(), e);
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    private void createMenus(JMenu main) {
        
        JMenuItem submenuItem;
        String rep = Singletons.getConfigurationValues().getScriptRepository();
        List<Object> list = new LinkedList<Object>();
        
        main.removeAll();
        
        submenuItem = new JMenuItem("Reload");
        submenuItem.setActionCommand(RELOAD_COMMAND);
        submenuItem.setIcon(Utilities.loadIcon("org/chuk/lee/scriptengine/reload.gif"
                , this.getClass().getClassLoader()));
        submenuItem.addActionListener(this);
        
        main.add(submenuItem);
        main.add(new JSeparator());        
        
        if ((rep == null) || (rep.trim().length() <= 0))
            return;
        
        list = getScriptsRecursively(list, rep + File.separator + "actions");
        
        main = createMenus(list, main);
/*
        for (File fn: getScriptsRecursively(list, rep + File.separator + "actions")) {
            submenuItem = new JMenuItem(fn.getName()
            , Utilities.loadIcon("org/chuk/lee/scriptengine/script.png", this.getClass().getClassLoader()));
            submenuItem.setActionCommand(fn.getAbsolutePath());
            submenuItem.addActionListener(this);
            main.add(submenuItem);
        }
 */
    }
    
    public JMenu createMenus(List<Object> list, JMenu menu) {
        File fn;
        JMenuItem sub;
        JMenu dir;
        
        for (Object obj: list) {
            if (obj instanceof File) {
                fn = (File)obj;
                sub = new JMenuItem(fn.getName()
                , Utilities.loadIcon("org/chuk/lee/scriptengine/script.png", this.getClass().getClassLoader()));
                sub.setActionCommand(fn.getAbsolutePath());
                sub.addActionListener(this);
                menu.add(sub);
                continue;
            }
            Map<File, List<Object>> map = (Map<File, List<Object>>)obj;
            for (File d: map.keySet()) {
                dir = new JMenu(d.getName());
                dir = createMenus(map.get(d), dir);
                menu.add(dir);
            }
        }
        
        return (menu);
    }
/*
    public static void dump(List<Object> list, String t) {
 
        File f;
 
        for (Object obj: list) {
            if (obj instanceof File) {
                f = (File)obj;
                System.out.println(t + f.getName());
                continue;
            }
            Map<File, List<Object>> map = (Map<File, List<Object>>)obj;
            for (File d: map.keySet()) {
                System.out.println(t + d.getName());
                dump(map.get(d), t + "  ");
            }
        }
    } */
}
