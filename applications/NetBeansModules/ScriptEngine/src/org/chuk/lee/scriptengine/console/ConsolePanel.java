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
 * ConsolePanel.java
 *
 * Created on June 30, 2006, 5:19 PM
 */

package org.chuk.lee.scriptengine.console;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.script.*;
import javax.swing.*;
import javax.swing.JFileChooser;
import org.chuk.lee.scriptengine.*;
import org.chuk.lee.scriptengine.api.NetBeansIDE;
import org.chuk.lee.scriptengine.api.AbstractScriptModule;
import org.openide.*;
import org.openide.windows.*;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author  projects
 */
public class ConsolePanel extends TopComponent implements ActionListener {
    
    private ScriptShellPanel shell = null;
    private CommandProcessor proc = null;
    private ScriptEngine engine = null;
    private JFileChooser fileChooser = null;
    private SessionLog logger = null;    
    
    /** Creates new form ConsolePanel */
    public ConsolePanel(CommandProcessor p, ScriptEngine e) {
        initComponents();
        proc = p;
        engine = e;
        shell = new ScriptShellPanel(proc);
        add(shell, BorderLayout.CENTER);
        clearButton.addActionListener(shell);
        logButton.addActionListener(this);
        ConsoleObject consObj = new ConsoleObject();
        consObj.setConsolePanel(this);
        e.put("Console", consObj);
        e.put("Engine", engine);
        e.put("NetBeans", new NetBeansIDE(engine));
        e.getContext().setWriter(new PrintWriter(shell.createWriter(), true));
        Mode mode = WindowManager.getDefault().findMode("output");
        mode.dockInto(this);
    }
    
    public void actionPerformed(ActionEvent aEvt) {
        String msg;
        
        if (logButton.isSelected()) {
            try {
                if (fileChooser == null)
                    fileChooser = new JFileChooser();
                if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                    logButton.setSelected(false);
                    shell.gainFocus();
                    return;
                }
                
                logger = new SessionLog(fileChooser.getSelectedFile(), "UTF-8");
                shell.startLog(logger);
            } catch (Exception e) {
                ErrorManager.getDefault().notify(e);
                logButton.setSelected(false);
                return;
            }
            msg = "Stop logging";
            
        } else {
            closeLog();
            msg = "Start logging";
        }
        
        shell.gainFocus();
        logButton.setToolTipText(msg);
    }
    
    protected void componentClosed() {
        if (!logButton.isSelected())
            return;
        closeLog();
    }
    
    private void closeLog() {
        shell.stopLog();
        try {
            logger.close();
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        } finally {
            try {
                logger.close();
            } catch (Exception e) { }
        }
        
        logger = null;
    }
    
    /*
     * Official method for Console object
     */
    
    protected void print(String s) {
        shell.print(s);
    }
    
    protected void flash() {
        requestAttention(true);
    }
    
    protected void startLog() {
        if (logButton.isSelected())
            return;
        logButton.setSelected(true);
        actionPerformed(null);
    }
    protected void startLog(String fn) throws IOException {
        if (logButton.isSelected())
            return;
        
        logger = new SessionLog(new File(fn), "UTF-8");
        
        logButton.setSelected(true);
        logButton.setToolTipText("Start logging");
        shell.startLog(logger);
        shell.gainFocus();
        
    }
    
    protected void stopLog() {
        if (!logButton.isSelected())
            return;
        logButton.setSelected(false);
        actionPerformed(null);
    }
    /*
     *End of official methods
     */
    
    public void display() {
        setDisplayName(proc.getName());
        open();
        requestActive();
        shell.gainFocus();
    }
    
    public int getPersistenceType() {
        return (TopComponent.PERSISTENCE_NEVER);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jPanel1 = new javax.swing.JPanel();
        logButton = new javax.swing.JToggleButton();
        clearButton = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        logButton.setFont(new java.awt.Font("Dialog", 1, 10));
        logButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/chuk/lee/scriptengine/console/execute.gif")));
        logButton.setText(org.openide.util.NbBundle.getMessage(ConsolePanel.class, "ConsolePanel.logButton.text")); // NOI18N
        logButton.setToolTipText(org.openide.util.NbBundle.getMessage(ConsolePanel.class, "ConsolePanel.logButton.toolTipText")); // NOI18N

        clearButton.setFont(new java.awt.Font("Dialog", 1, 10));
        clearButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/chuk/lee/scriptengine/console/clean.gif")));
        clearButton.setText(org.openide.util.NbBundle.getMessage(ConsolePanel.class, "ConsolePanel.clearButton.text")); // NOI18N
        clearButton.setToolTipText(org.openide.util.NbBundle.getMessage(ConsolePanel.class, "ConsolePanel.clearButton.toolTipText")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(logButton, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(clearButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(logButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(clearButton)
                .addContainerGap(242, Short.MAX_VALUE))
        );
        add(jPanel1, java.awt.BorderLayout.EAST);

    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton clearButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JToggleButton logButton;
    // End of variables declaration//GEN-END:variables
    
}
