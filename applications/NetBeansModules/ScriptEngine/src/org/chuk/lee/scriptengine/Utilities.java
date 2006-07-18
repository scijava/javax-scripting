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
 * Utilities.java
 *
 * Created on June 25, 2006, 7:06 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.chuk.lee.scriptengine;

import java.io.*;
import java.io.IOException;

import static java.io.File.*;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.openide.ErrorManager;

/**
 *
 * @author projects
 */
public class Utilities {    
    
    public static final String[] SCRIPT_DIRECTORIES = {
        "actions", "load", "console"
    };
    
    public static String concat(List<String> l) {
        StringBuilder b = new StringBuilder();
        for (String s: l)
            b.append(s).append("\n");
        return (b.toString().trim());
    }
    
    public static Icon loadIcon(String n, ClassLoader loader) {
        
        ImageIcon i = null;
        java.awt.Image img;
        
        img = loadImage(n, loader);
        if (img != null)
            return (new ImageIcon(img));
        
        return (null);
    }
    
    public static java.awt.Image loadImage(String n, ClassLoader loader) {
        try {
            return (ImageIO.read(loader.getResourceAsStream(n)));
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
        
        return (null);
    }
    
    public static void initializeRepository(String r) throws Exception {
        
        File f = new File(r);
        
        if (!f.isDirectory())
            throw new IOException(r + " is not a directory");
        
        for (String sd: SCRIPT_DIRECTORIES) {
            File nd = new File(r + separator + sd);
            if (nd.isDirectory())
                continue;
            if (!nd.mkdirs())
                throw new IOException("Cannot create " + nd.getAbsolutePath());
        }
    }
    
    public static boolean isConfigurationValid() {
        
        return (validRepository(Singletons.getConfigurationValues().getScriptRepository()));
    }
    
    public static boolean validRepository(String rep) {
        if ((rep == null) || (rep.trim().length() <= 0))
            return (false);
        
        File f = new File(rep);
        
        return (f.isDirectory());
    }
    
    public static List<Object> getScriptsRecursively(List<Object> top, String rep) {
        
        File f = new File(rep);
        List<Object> list;
        Map<File, List<Object>> dir;
        
        if (f.isFile()) {
            top.add(f);
            return (top);
        }
        
        for (File ff: f.listFiles()) {
            if (ff.isFile()) {
                top.add(ff);
                continue;
            }
            list = new LinkedList<Object>();
            list = getScriptsRecursively(list, ff.getAbsolutePath());
            dir = new HashMap<File, List<Object>>();
            dir.put(ff, list);
            top.add(dir);
        }
        
        return (top);
    }
    
    public static File[] getScripts(String rep) {
        String[] s  = null;
        File[] flist = null;
        File act = new File(rep);
        
        if (!act.isDirectory())
            return (new File[0]);
        
        return (act.listFiles(new FileFilter() {
            public boolean accept(File f) {
                String fn = f.getName();
                return (f.isFile() && (!fn.endsWith("~")) && (!fn.startsWith(".")));
            }
        }));
    }
}
