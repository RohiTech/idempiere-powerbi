package org.idempiere.powerbi;

import java.math.BigDecimal;
import java.security.Timestamp;
import java.sql.SQLException;
import java.util.Vector;
import java.util.logging.Level;

import javax.sql.RowSet;

import org.compiere.minigrid.IMiniTable;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;
import org.compiere.util.Util;

public class PowerBI
{
	public static CLogger log = CLogger.getCLogger(PowerBI.class);
	
	// Obtencion de los datos proporcionados de la base de datos
	
	public Vector<Vector<Object>> getInvoiceList()
	{
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		
		String sql = "select\r\n"
				+ "	f.c_invoice_id as factura_id,\r\n"
				+ "	f.documentno as num_factura,\r\n"
				+ "	f.dateacct::date as fecha_factura,\r\n"
				+ "	bp.name as tercero,\r\n"
				+ "	m.iso_code as moneda,\r\n"
				+ "	f.grandtotal as total\r\n"
				+ "from c_invoice f\r\n"
				+ "	join c_bpartner bp\r\n"
				+ "	on f.c_bpartner_id = bp.c_bpartner_id \r\n"
				+ "	join c_currency m\r\n"
				+ "	on f.c_currency_id = m.c_currency_id \r\n"
				+ "where f.ad_client_id = 1000000\r\n"
				+ "	and f.issotrx = 'Y' \r\n"
				+ "	and f.docstatus = 'CO';";
		
		RowSet rs = DB.getRowSet(sql);
		
		try
		{
			while(rs.next())
			{
				Vector<Object> line = new Vector<Object>();
				
				line.add(new Boolean(false)); // 0-Seleccion
				
				KeyNamePair pp = new KeyNamePair(rs.getInt(1), rs.getString(2));
				
				line.add(pp); // 1-Numero de Factura (DocumentNo) => ID Factura
				
				line.add(rs.getTimestamp(3)); // 2-Fecha de Factura (DateAcct)
				
				line.add(rs.getString(4)); // 3-Cliente (BusinessPartner)
				
				line.add(rs.getString(5)); // 4-Moneda
				
				BigDecimal total = rs.getBigDecimal(6); // Total de la Factura (GrandTotal)
				
				// Evaluar si tiene valor, sino omitir dicha factura y continuar mostrando facturas
				if(total == null || total.signum() == 0)
					continue;
				
				line.add(total);
				
				data.add(line);
			}
		}
		catch(SQLException e)
		{
			log.log(Level.SEVERE, sql, e);
		}
		
		return data;
	}
	
	// En este metodo vamos a definir los nombres de las columnas
	
	public Vector<String> getInvoiceListColumnNames()
	{
		Vector<String> columnNames = new Vector<String>();
		
		// Utilizamos metodos para encontrar la traduccion segun diccionario
		columnNames.add(Msg.getMsg(Env.getCtx(), "Select"));
		columnNames.add(Util.cleanAmp(Msg.translate(Env.getCtx(), "DocumentNo")));
		columnNames.add(Msg.translate(Env.getCtx(), "Date"));
		
		// Establecemos valores de manera directa
		columnNames.add("Cliente");
		columnNames.add("Moneda");
		columnNames.add("Total");
		
		return columnNames;
	}
	
	// Agregar columnas en el modelo de la tabla
	
	public void setInvoiceListColumnClass(IMiniTable invoiceTable)
	{
		int k = 0;
		
		invoiceTable.setColumnClass(k++, Boolean.class, false); // 0-Seleccion
		invoiceTable.setColumnClass(k++, String.class, false); // 1-DocumentNo
		invoiceTable.setColumnClass(k++, Timestamp.class, true); // 2-DateAcct
		invoiceTable.setColumnClass(k++, String.class, true); // 3-Tercero
		invoiceTable.setColumnClass(k++, String.class, true); // 4-Moneda
		invoiceTable.setColumnClass(k++, BigDecimal.class, true); // 5-Total
		
		invoiceTable.autoSize();
	}
}
