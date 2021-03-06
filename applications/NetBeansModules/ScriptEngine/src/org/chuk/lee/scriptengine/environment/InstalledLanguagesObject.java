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
 * InstalledLanguagesObject.java
 *
 * Created on July 3, 2006, 1:52 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.chuk.lee.scriptengine.environment;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import javax.script.ScriptEngineFactory;
import org.chuk.lee.scriptengine.ConfigurationValues;
import org.chuk.lee.scriptengine.ServiceLoaderUtilities;
import org.chuk.lee.scriptengine.Singletons;
import org.openide.nodes.*;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author projects
 */
public class InstalledLanguagesObject extends Children.Keys implements PropertyChangeListener {
    
    /**
     * Creates a new instance of InstalledLanguagesObject
     */
    public InstalledLanguagesObject() {
        Singletons.getConfigurationValues().addPropertyChangeListener(this);
    }
    
    protected void addNotify() {
        load();
    }
    
    protected void removeNotify() {
        setKeys(Collections.EMPTY_SET);
    }
    
    private void load() {
        ServiceLoaderUtilities service = Singletons.getServiceLoader();
        Iterator<ScriptEngineFactory> iter = service.getScriptEngines();
        List<ScriptEngineFactory> list = new LinkedList<ScriptEngineFactory>();
        
        while (iter.hasNext())
            list.add(iter.next());
        
        setKeys(list);
    }
    
    protected Node[] createNodes(Object object) {
        ScriptEngineFactory fac = (ScriptEngineFactory)object;
        AbstractNode node = new ScriptLanguageObjectNode(new ScriptLanguageObject(fac));
        return (new Node[]{ node });
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (!evt.getPropertyName().equals(ConfigurationValues.ENGINE_LIST))
            return;
        Thread t = new Thread(new Runnable() {
            public void run() {
                load();
            }
        });
        t.start();
    }
}
