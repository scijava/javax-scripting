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
 * NetBeanIDE.java
 *
 * Created on June 26, 2006, 4:49 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.chuk.lee.scriptengine.api;

import java.io.IOException;
import java.net.*;
import java.util.*;
import javax.script.*;
import javax.script.ScriptEngine;
import javax.swing.JOptionPane;
import org.chuk.lee.scriptengine.*;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.StatusDisplayer;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.*;
import org.openide.util.lookup.*;

/**
 *
 * @author projects
 */
public class NetBeansIDE extends AbstractScriptModule {
    
    public static final String MODULE_NAME = "NetBeans";
    public static final String STARTUP_EVENT = "NetBeans.startup";
    public static final String SHUTDOWN_EVENT = "NetBeans.shutdown";
    public static final String[] EVENT_NAMES = { STARTUP_EVENT, SHUTDOWN_EVENT };
    
    private NetBeansIDE ide = null;
    private ScriptEngine engine;
    
    /** Creates a new instance of NetBeanIDE */
    public NetBeansIDE(ScriptEngine e) {
        engine = e;
    }
    public NetBeansIDE() { }
    
    /*
     * Locate a module by its user friendly name
     */
    public ScriptModule findByName(String c) {
        
        Lookup.Result result = Lookup.getDefault().lookup(new Lookup.Template(ScriptModule.class));
        ScriptModule sm;
        
        for (Object o: result.allInstances()) {
            sm = (ScriptModule)o;
            if (sm.getName().equals(c))
                return (initModule(sm));
        }
        
        return (null);
    }
    
    /*
     *Locate a module by its fully qualified class name
     */
    public ScriptModule findByClass(String c) {
        
        Lookup.Result result = Lookup.getDefault().lookup(new Lookup.Template(ScriptModule.class));
        
        for (Object o: result.allInstances()) {
            if (o.getClass().getName().equals(c))
                return (initModule((ScriptModule)o));
        }
        
        return (null);
    }
    private ScriptModule initModule(ScriptModule module) {
        module.initalize(engine);
        return (module);
    }
    
    /*
     *Display a message at the status line in NetBeans
     */
    public void setStatusText(String msg) {
        StatusDisplayer.getDefault().setStatusText(msg);
    }
    /*
     *Create an display an information dialog.
     */
    public void informationDialog(String msg, String title) {
        showDialog(JOptionPane.INFORMATION_MESSAGE, msg, title);
    }
    /*
     *Create an display a warning dialog
     */
    public void warningDialog(String msg, String title) {
        showDialog(JOptionPane.WARNING_MESSAGE, msg, title);
    }
    /*
     *Create an display an error dialog
     */
    public void errorDialog(String msg, String title) {
        showDialog(JOptionPane.ERROR_MESSAGE, msg, title);
    }
    
    /*
     *Create an display an confirmation dialog with OK and CANCEL button
     *Returns either 1 for OK or 0 for CANCEL
     */
    public int showConfirmationDialog(String msg, String title) {
        
        String t = ((title == null) || (title.trim().length() <= 0))? "Please confirm" : title;
        
        int ok = JOptionPane.showConfirmDialog(null, msg, t, JOptionPane.OK_CANCEL_OPTION);
        
        return ((ok == JOptionPane.OK_OPTION)? 1: 0);
    }
    /*
     *Create an display a dialog box to solicit a line of text
     */
    public String showInputLineDialog(String msg) {
        
        return (JOptionPane.showInputDialog(null, msg));
    }
    
    private void showDialog(int i, String msg, String title) {
        if ((title == null) || (title.trim().length() <= 0))
            switch (i) {
                default:
                case JOptionPane.INFORMATION_MESSAGE:
                    title = "Information";
                    break;
                case JOptionPane.WARNING_MESSAGE:
                    title = "Warning";
                    break;
                case JOptionPane.ERROR_MESSAGE:
                    title = "Error";
                    break;
            }
            JOptionPane.showMessageDialog(null, msg, title, i);
    }
    
    /*
     *Store some data in the session data. Analogous to HttpSession object.
     *Does not presist over multiple NetBeans session viz. start/exit.
     *Similar to HashMap or Hashtable in use
     */
    public Object putSessionData(String k, Object v) {
        return (Singletons.getSessionStorage().put(k, v));
    }
    /*
     *Retrieves a piece of session data based on the key
     */
    public Object getSessionData(String k) {
        return (Singletons.getSessionStorage().get(k));
    }
    
    /*
     *Stores a piece of data in persistent storage. Data can be persisted
     *over multiple session. Set null to clear data
     */
    public Object putApplicationData(String k, Object v) {
        return (Singletons.getPersistentStorage().save(k, v));
    }
    /*
     *Retrieves a piece of data from the presistent store
     */
    public Object getApplicationData(String k) {
        return (Singletons.getPersistentStorage().load(k));
    }
    
    /*
     *Get the fully path name of the script repository
     */
    public String getScriptRepository() {
        return (Singletons.getConfigurationValues().getScriptRepository());
    }
    
    /*
     *Execute an external program. cmd is the full path name of the command and
     *args are arguments to cmd
     */
    public Process execute(String cmd, String args) {
        Process proc = null;
        NbProcessDescriptor process = new NbProcessDescriptor(cmd, args);
        try {
            proc = process.exec();
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
            return (null);
        }
        return (proc);
    }
    /*
     *Execute a command without arguments
     */
    public Process execute(String cmd) {
        return (execute(cmd, ""));
    }
    
    /*
     *Display a URL using NetBeans default configured browser
     */
    public void showURL(String s) {
        try {
            HtmlBrowser.URLDisplayer.getDefault().showURL(new URL(s));
        } catch (Exception e) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(
                    e.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
    }
    
    public String[] list() {
        return (EVENT_NAMES);
    }
    
    public String getName() {        
        return (MODULE_NAME);
    }
    
    //Do not really need to call this. NetBeans object is automatically 
    //added to all scripting environments
    public void initalize(ScriptEngine e) {
        engine = e;
        Bindings bin = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        bin.put(MODULE_NAME, this);
        engine.setBindings(bin, ScriptContext.ENGINE_SCOPE);
    }
    
}
