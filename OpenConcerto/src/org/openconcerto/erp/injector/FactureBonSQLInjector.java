/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2011 OpenConcerto, by ILM Informatique. All rights reserved.
 * 
 * The contents of this file are subject to the terms of the GNU General Public License Version 3
 * only ("GPL"). You may not use this file except in compliance with the License. You can obtain a
 * copy of the License at http://www.gnu.org/licenses/gpl-3.0.html See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each file.
 */
 
 package org.openconcerto.erp.injector;

import java.math.BigDecimal;
import java.util.Collection;

import org.openconcerto.sql.model.DBRoot;
import org.openconcerto.sql.model.SQLInjector;
import org.openconcerto.sql.model.SQLRowAccessor;
import org.openconcerto.sql.model.SQLRowValues;
import org.openconcerto.sql.model.SQLTable;

public class FactureBonSQLInjector extends SQLInjector {
    public FactureBonSQLInjector(final DBRoot root) {
        super(root, "SAISIE_VENTE_FACTURE", "BON_DE_LIVRAISON", true);
        final SQLTable tableFacture = getSource();
        final SQLTable tableBon = getDestination();
        map(tableFacture.getField("ID_CLIENT"), tableBon.getField("ID_CLIENT"));
        mapDefaultValues(tableBon.getField("SOURCE"), tableFacture.getName());
        map(tableFacture.getKey(), tableBon.getField("IDSOURCE"));
        if (tableFacture.contains("ID_POLE_PRODUIT") && tableBon.contains("ID_POLE_PRODUIT")) {
            map(tableFacture.getField("ID_POLE_PRODUIT"), tableBon.getField("ID_POLE_PRODUIT"));
        }
    }

    @Override
    protected void merge(SQLRowAccessor srcRow, SQLRowValues rowVals) {
        super.merge(srcRow, rowVals);

        // Merge elements
        final SQLTable tableElementSource = getSource().getTable("SAISIE_VENTE_FACTURE_ELEMENT");
        final SQLTable tableElementDestination = getSource().getTable("BON_DE_LIVRAISON_ELEMENT");
        final Collection<? extends SQLRowAccessor> myListItem = srcRow.asRow().getReferentRows(tableElementSource);
        transfertReference(srcRow, rowVals, "NOM", "NOM");
        transfertReference(srcRow, rowVals, "INFOS", "INFOS");
        transfertNumberReference(srcRow, rowVals, tableElementDestination, "ID_BON_DE_LIVRAISON");

        if (myListItem.size() != 0) {
            final SQLInjector injector = SQLInjector.getInjector(tableElementSource, tableElementDestination);
            for (SQLRowAccessor rowElt : myListItem) {
                final SQLRowValues createRowValuesFrom = injector.createRowValuesFrom(rowElt.asRow());
                if (createRowValuesFrom.getTable().getFieldsName().contains("POURCENT_ACOMPTE")) {
                    if (createRowValuesFrom.getObject("POURCENT_ACOMPTE") == null) {
                        createRowValuesFrom.put("POURCENT_ACOMPTE", new BigDecimal(100.0));
                    }
                }
                createRowValuesFrom.put("ID_BON_DE_LIVRAISON", rowVals);
            }
        }
    }
}
