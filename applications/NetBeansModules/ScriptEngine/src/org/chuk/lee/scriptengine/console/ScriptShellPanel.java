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
 * @(#)ScriptShellPanel.java	1.1 06/04/19 08:43:44
 *
 * Copyright (c) 2006 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * -Redistribution of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 *
 * -Redistribution in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */

package org.chuk.lee.scriptengine.console;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import org.openide.awt.StatusDisplayer;


/**
 * A JPanel subclass containing a scrollable text area displaying the
 * jconsole's script console.
 */
class ScriptShellPanel extends JPanel implements ActionListener {
    
    public class ConsoleMapper extends Writer {
        private StringBuilder str = new StringBuilder();
        public void write(char[] cbuf, int off, int len) {
            str.append(new String(cbuf, off, len));
        }
        
        public void flush() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    print(str.toString());
                    str = new StringBuilder();
                }
            });
        }
        
        public void close() { }
    }
    
    // my script command processor
    private CommandProcessor commandProcessor;
    // editor component for command editing
    private JTextComponent editor;
    
    // document management
    private boolean updating;
    private int     mark;
    private String  curText;  // handles multi-line input via '\'
    private EditableAtEndDocument editableDoc = null;
    private SessionLog logger = null;
    
    public ScriptShellPanel(CommandProcessor cmdProc) {
        setLayout(new BorderLayout());
        this.commandProcessor = cmdProc;
        this.editor = new JTextArea();
        editableDoc = new EditableAtEndDocument();
        editor.setDocument(editableDoc);
        JScrollPane scroller = new JScrollPane();
        scroller.getViewport().add(editor);
        add(scroller, BorderLayout.CENTER);
        
        //editor.getDocument().addDocumentListener(new DocumentListener() {
        editableDoc.addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
            }
            
            public void insertUpdate(DocumentEvent e) {
                if (updating) return;
                beginUpdate();
                editor.setCaretPosition(editor.getDocument().getLength());
                if (insertContains(e, '\n')) {
                    String cmd = getMarkedText();
                    // Handle multi-line input
                    if ((cmd.length() == 0) ||
                            (cmd.charAt(cmd.length() - 1) != '\\')) {
                        // Trim "\\n" combinations
                        cmd = trimContinuations(cmd);
                        final String result;
                        result = executeCommand(cmd);
                        
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                if (result != null) {
                                    print(result + "\n");
                                }
                                printPrompt();
                                setMark();
                                endUpdate();
                            }
                        });
                    }
                } else {
                    endUpdate();
                }
            }
            
            public void removeUpdate(DocumentEvent e) {
            }
        });
        
        // This is a bit of a hack but is probably better than relying on
        // the JEditorPane to update the caret's position precisely the
        // size of the insertion
        editor.addCaretListener(new CaretListener() {
            public void caretUpdate(CaretEvent e) {
                int len = editableDoc.getLength();
                if (e.getDot() > len) {
                    editor.setCaretPosition(len);
                }
            }
        });
        
        clear();
    }
    
    public Writer createWriter() {
        return (new ConsoleMapper());
    }
    
    public void gainFocus() {
        editor.requestFocusInWindow();
    }
    
    public void startLog(SessionLog l) {
        logger = l;
    }
    public void stopLog() {
        logger = null;
    }
    
    public void actionPerformed(ActionEvent aEvt) {
        clear();
    }
    
    public void requestFocus() {
        editor.requestFocus();
    }
    
    public void clear() {
        clear(true);
    }
    
    public void clear(boolean prompt) {
        //EditableAtEndDocument d = (EditableAtEndDocument) editor.getDocument();
        editableDoc.clear();
        if (prompt) printPrompt();
        setMark();
        editor.requestFocus();
    }
    
    public void setMark() {
        //((EditableAtEndDocument) editor.getDocument()).setMark();
        editableDoc.setMark();
    }
    
    public String getMarkedText() {
        try {
            //String s = ((EditableAtEndDocument) editor.getDocument()).getMarkedText();
            String s = editableDoc.getMarkedText();
            int i = s.length();
            while ((i > 0) && (s.charAt(i - 1) == '\n')) {
                i--;
            }
            return s.substring(0, i);
        } catch (BadLocationException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public void print(String s) {
        //Document d = editor.getDocument();
/*
        try {
            editableDoc.insertString(editableDoc.getLength(), s, null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        } */
        
        try {
            editableDoc.insertString(editableDoc.getLength(), logIt(s), null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    
    //
    // Internals only below this point
    //
    
    private String logIt(String s) {
        if (logger != null)
            try {
                logger.print(s);
            } catch (Exception e) {
                s += s + "\t[Warning logging error:" + e.getMessage() + "]";
            }
        return (s);
    }
    
    private String executeCommand(String cmd) {
        
        logIt(cmd + "\n");
        
        return (commandProcessor.executeCommand(cmd));
    }
    
    private String getPrompt() {
        return commandProcessor.getPrompt();
    }
    
    private void beginUpdate() {
        updating = true;
    }
    
    private void endUpdate() {
        updating = false;
    }
    
    private void printPrompt() {
        print(getPrompt());
    }
    
    private boolean insertContains(DocumentEvent e, char c) {
        String s = null;
        try {
            s = editor.getText(e.getOffset(), e.getLength());
            for (int i = 0; i < e.getLength(); i++) {
                if (s.charAt(i) == c) {
                    return true;
                }
            }
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
        return false;
    }
    
    private String trimContinuations(String text) {
        int i;
        while ((i = text.indexOf("\\\n")) >= 0) {
            text = text.substring(0, i) + text.substring(i+2, text.length());
        }
        return text;
    }
    
    public static void main(String[] args) throws Exception {
        
    }
}
