/**
 * 
 */
package com.yacme.ext.oxsit.test.ooo;


import com.sun.star.awt.Size;
import com.sun.star.beans.Property;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.beans.XPropertySetInfo;
import com.sun.star.container.XIndexAccess;
import com.sun.star.container.XNameAccess;
import com.sun.star.container.XNameContainer;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XController;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XModel;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.style.BreakType;
import com.sun.star.style.XStyle;
import com.sun.star.style.XStyleFamiliesSupplier;
import com.sun.star.text.WrapTextMode;
import com.sun.star.text.XPagePrintable;
import com.sun.star.text.XText;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextRange;
import com.sun.star.text.XTextTable;
import com.sun.star.text.XTextTablesSupplier;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.text.XTextViewCursorSupplier;
import com.sun.star.ucb.XContent;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Exception;
import com.sun.star.uno.Type;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.yacme.ext.oxsit.Utilities;
import com.yacme.ext.oxsit.ooo.GlobConstant;
import com.yacme.ext.oxsit.pcsc.CardInfoOOo;

/**
 * @author beppe
 *
 */
public class TestWriterReportCreation {

	public OOoServerInfo SvrInfo = new OOoServerInfo();
    private static TestRootVerifierOOo theInstance = null;
    public XComponent xComponent = null;
    private XContent xContent = null;
    private Object XActiveDataSink;
    private XMultiComponentFactory xMCF = null;
    private XComponentContext xCC = null;

	public TestWriterReportCreation() {
		
	}

	/**
	 * @param viewCursor
	 * @param string
	 */
	private void insertAHeading(XTextViewCursor xViewCursor, String string, String style) {
		// TODO Auto-generated method stub
		//from view cursor get properties
		try {
			XText xDocumentText = xViewCursor.getText();
		     
		     // the text creates a model cursor from the viewcursor
		     XTextCursor xModelCursor = xDocumentText.createTextCursorByRange(xViewCursor.getStart());
	
		     // query its XPropertySet interface, we want to set character and paragraph properties
		     XPropertySet xCursorPropertySet = (XPropertySet)UnoRuntime.queryInterface(
		         XPropertySet.class, xViewCursor);
		     String sParSt = (String) xCursorPropertySet.getPropertyValue("ParaStyleName");
		     xCursorPropertySet.setPropertyValue("ParaStyleName", new String(style) );
		     xViewCursor.setString(string+"\r");
		     xViewCursor.collapseToEnd();
		     xViewCursor.setString("\r");
		     xViewCursor.collapseToEnd();
		     xCursorPropertySet.setPropertyValue("ParaStyleName", sParSt );
		     		     
		     if(xCursorPropertySet != null) {
//		    	 XPropertySetInfo xf = xCursorPropertySet.getPropertySetInfo();
//		    	 Property[] pr = xf.getProperties();
//		    	 for(int i = 0; i < pr.length;i++)
//		    		 trace(""+pr[i].Name);
			     Utilities.showProperties(xViewCursor, xCursorPropertySet);		    	 
		     }
		     xCursorPropertySet = (XPropertySet)UnoRuntime.queryInterface(
			         XPropertySet.class, xModelCursor);	     
//		     Utilities.showProperties(xModelCursor, xCursorPropertySet);
/*		     sParSt = (String) xCursorPropertySet.getPropertyValue("ParaStyleName");
		     xCursorPropertySet.setPropertyValue("ParaStyleName", new String("Heading 1") );
		     xModelCursor.setString(string+"\r");
		     xModelCursor.collapseToEnd();
		     xModelCursor.setString("\r");
		     xCursorPropertySet.setPropertyValue("ParaStyleName", sParSt );*/
		     
		} catch (Throwable e) {
			trace(e);
		}
		
		
   	}

	/** This method sets the text colour of the cell refered to by sCellName to white and inserts
    the string sText in it
 */
private void insertIntoCell(String sCellName, String sText, XTextTable xTable) {
    // Access the XText interface of the cell referred to by sCellName
    XText xCellText = (XText) UnoRuntime.queryInterface(
        XText.class, xTable.getCellByName(sCellName));

    // create a text cursor from the cells XText interface
    XTextCursor xCellCursor = xCellText.createTextCursor();

    // Get the property set of the cell's TextCursor
    XPropertySet xCellCursorProps = (XPropertySet)UnoRuntime.queryInterface(
        XPropertySet.class, xCellCursor);

/*    try {
        // Set the colour of the text to white
        xCellCursorProps.setPropertyValue("CharColor", new Integer(16777215));
    } catch (Exception e) {
        e.printStackTrace(System.out);
    }*/

    // Set the text in the cell to sText
    xCellText.setString(sText);
}

	 /** This method shows how to create and insert a text table, as well as insert text and formulae
    into the cells of the table
	 * @param xTxCurs TODO
 */
protected XTextTable insertTable(XTextDocument xDoc, XTextCursor xTxCurs, int row, int col)
{
    try 
    {
    	XMultiServiceFactory xMSF = (XMultiServiceFactory)UnoRuntime.queryInterface(XMultiServiceFactory.class, xDoc);

//        Object desktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", xCC);
        // get the remote service manager
        // query its XDesktop interface, we need the current component
//        XDesktop xDesktop = (XDesktop)UnoRuntime.queryInterface(XDesktop.class, desktop);
    	
//        XComponent xWriterComponent = xDesktop.getCurrentComponent();

//        XModel xModel = (XModel)UnoRuntime.queryInterface(XModel.class, xDoc);

//        XController xController = xModel.getCurrentController();
        // the controller gives us the TextViewCursor
//        XTextViewCursorSupplier xViewCursorSupplier =
//        (XTextViewCursorSupplier)UnoRuntime.queryInterface(XTextViewCursorSupplier.class, xController);
//        XTextViewCursor xViewCursor = xViewCursorSupplier.getViewCursor();
        //get the main text document
        XText mxDocText = xDoc.getText();      

        // Create a new table from the document's factory
        XTextTable xTable = (XTextTable) UnoRuntime.queryInterface( 
            XTextTable.class, 
            xMSF.createInstance(
                "com.sun.star.text.TextTable" ) );
        
        // Specify that we want the table to have 4 rows and 4 columns
        xTable.initialize( row	, col );

        XTextRange xPos = xTxCurs.getStart();//xViewCursor.getStart();

        // Insert the table into the document
        mxDocText.insertTextContent( xPos, xTable, false);
        // Get an XIndexAccess of the table rows
        XIndexAccess xRows = xTable.getRows();
        
        // Access the property set of the first row (properties listed in service description:
        // com.sun.star.text.TextTableRow)
		// Access the property set of the whole table
	       XPropertySet xTableProps = (XPropertySet)UnoRuntime.queryInterface( 
	           XPropertySet.class, xTable );

//	       Utilities.showProperties(xTable, xTableProps);
	 //set table
	// We want visible background colors
	       xTableProps.setPropertyValue( "RepeatHeadline", new Boolean(true));
	       xTableProps.setPropertyValue( "HeaderRowCount", new Integer(1));
	       xTableProps.setPropertyValue( "HoriOrient", new Short(com.sun.star.text.HoriOrientation.LEFT));
	       xTableProps.setPropertyValue( "RelativeWidth", new Short((short)100));
	       Object oProp = xTableProps.getPropertyValue("TextWrap");
	       
	       WrapTextMode aMode = (WrapTextMode) AnyConverter.toObject(WrapTextMode.class, oProp); 
        trace("WrapTextMode "+aMode.getValue());
        
        	BreakType  abk = (BreakType) AnyConverter.toObject(BreakType.class,  xTableProps.getPropertyValue("BreakType"));
            trace("BreakType "+abk.getValue());
            
            XNameContainer xnc = (XNameContainer) AnyConverter.toObject(XNameContainer.class, 
            		xTableProps.getPropertyValue("UserDefinedAttributes") );
            
            String[] en = xnc.getElementNames();
            if(en != null) {
            	for(int i = 0; i <en.length; i++) {
                	trace(en[i]);            		
            	}
            }
	       
//	       xTableProps.setPropertyValue( "Split", new Boolean(false));
//	       xTableProps.setPropertyValue( "IsWidthRelative", new Boolean(true));

//	       Utilities.showProperties(xTable, xTableProps);
	       
	       
        // If BackTransparant is false, then the background color is visible
//        xRow.setPropertyValue( "BackTransparent", new Boolean(false));
        // Specify the color of the background to be dark blue
//        xRow.setPropertyValue( "BackColor", new Integer(6710932));
        
        // Access the property set of the whole table
//        XPropertySet xTableProps = (XPropertySet)UnoRuntime.queryInterface( 
//            XPropertySet.class, xTable );
        // We want visible background colors
//        xTableProps.setPropertyValue( "BackTransparent", new Boolean(false));
        // Set the background colour to light blue
//        xTableProps.setPropertyValue( "BackColor", new Integer(13421823));
        
        // set the text (and text colour) of all the cells in the first row of the table
        insertIntoCell( "A1", "First Row", xTable );
        insertIntoCell( "B1", "First Row (B)", xTable );
        insertIntoCell( "C1", "First Row (C)", xTable );
        insertIntoCell( "A2", "Second Row", xTable );
        insertIntoCell( "A3", "Third Row", xTable );
        insertIntoCell( "A4", "Row", xTable );
        
 /*       // Insert random numbers into the first this three cells of each
        // remaining row
        xTable.getCellByName( "A2" ).setValue( getRandomDouble() );
        xTable.getCellByName( "B2" ).setValue( getRandomDouble() );
        xTable.getCellByName( "C2" ).setValue( getRandomDouble() );
        
        xTable.getCellByName( "A3" ).setValue( getRandomDouble() );
        xTable.getCellByName( "B3" ).setValue( getRandomDouble() );
        xTable.getCellByName( "C3" ).setValue( getRandomDouble() );
        
        xTable.getCellByName( "A4" ).setValue( getRandomDouble() );
        xTable.getCellByName( "B4" ).setValue( getRandomDouble() );
        xTable.getCellByName( "C4" ).setValue( getRandomDouble() );*/
        
        // Set the last cell in each row to be a formula that calculates
        // the sum of the first three cells
/*        xTable.getCellByName( "D2" ).setFormula( "sum <A2:C2>" );
        xTable.getCellByName( "D3" ).setFormula( "sum <A3:C3>" );
        xTable.getCellByName( "D4" ).setFormula( "sum <A4:C4>" );*/
        
        // first query the XTextTablesSupplier interface from our document
        XTextTablesSupplier xTablesSupplier = (XTextTablesSupplier) UnoRuntime.queryInterface(
            XTextTablesSupplier.class, xDoc );
        // get the tables collection
        XNameAccess xNamedTables = xTablesSupplier.getTextTables();
        
        // now query the XIndexAccess from the tables collection
 /*       XIndexAccess xIndexedTables = (XIndexAccess) UnoRuntime.queryInterface(
            XIndexAccess.class, xNamedTables);

        // we need properties
        xTableProps = null;
        */
        
/*        XTextTable xt =	(XTextTable)UnoRuntime.queryInterface(XTextTable.class,        xNamedTables.getByName("Table1") );
        xTableProps = (XPropertySet)UnoRuntime.queryInterface( 
        		XPropertySet.class, xt );

        Utilities.showProperties(xt, xTableProps);

        oProp = xTableProps.getPropertyValue("TextWrap");

        aMode = (WrapTextMode) AnyConverter.toObject(WrapTextMode.class, oProp); 
        trace("WrapTextMode "+aMode.getValue());
        abk = (BreakType) AnyConverter.toObject(BreakType.class,  xTableProps.getPropertyValue("BreakType"));
        trace("BreakType "+abk.getValue());*/
        // get the tables
        /*        for (int i = 0; i < xIndexedTables.getCount(); i++) {
            Object table = xIndexedTables.getByIndex(i);
            // the properties, please!
            xTableProps = (XPropertySet) UnoRuntime.queryInterface(
                XPropertySet.class, table);

            // color the table light green in format 0xRRGGBB
            xTableProps.setPropertyValue("BackColor", new Integer(0xC8FFB9));
        } */

        return xTable;
    } 
    catch (Exception e) 
    {
        e.printStackTrace ( System.out );
    }
    return null;
}

	private void trace(String s) {
		System.out.println(s);
	}
	
	private void trace(Throwable s) {
		s.printStackTrace();
	}

	
	private void setCustomPageStyle(XTextDocument _xaDoc) {
		XText xText = (com.sun.star.text.XText) _xaDoc.getText();

		XStyleFamiliesSupplier StyleFam = (XStyleFamiliesSupplier) UnoRuntime.queryInterface(XStyleFamiliesSupplier.class, _xaDoc);
		XNameAccess StyleFamNames = StyleFam.getStyleFamilies();

		XStyle StdStyle = null;
		try {
			XNameAccess PageStyles = (XNameAccess) AnyConverter.toObject(new Type(XNameAccess.class),StyleFamNames.getByName("PageStyles"));
			StdStyle = (XStyle) AnyConverter.toObject(new Type(XStyle.class),
					PageStyles.getByName("Standard"));

			XPropertySet PropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, StdStyle);

			// changing/getting some properties
			PropSet.setPropertyValue("IsLandscape", new Boolean(true));
			Size aSz = new Size();
			aSz.Height = 21000;
			aSz.Width = 29700;
			PropSet.setPropertyValue("Size", aSz);
			
			PropSet.setPropertyValue("LeftMargin", new Integer(1500));
			PropSet.setPropertyValue("RightMargin", new Integer(1500));
			PropSet.setPropertyValue("TopMargin", new Integer(1500));
			PropSet.setPropertyValue("BottomMargin", new Integer(1500));
		}
		catch (Exception e) {
			e.printStackTrace();
		} 
		
	}
	
	private void prepareAHeader(XTextDocument _xaDoc, String _TheHeader) {
		XText xText = (com.sun.star.text.XText) _xaDoc.getText();

		XStyleFamiliesSupplier StyleFam = (XStyleFamiliesSupplier) UnoRuntime.queryInterface(XStyleFamiliesSupplier.class, _xaDoc);
		XNameAccess StyleFamNames = StyleFam.getStyleFamilies();

		XStyle StdStyle = null;
		try {
			XNameAccess PageStyles = (XNameAccess) AnyConverter.toObject(new Type(XNameAccess.class),StyleFamNames.getByName("PageStyles"));
			StdStyle = (XStyle) AnyConverter.toObject(new Type(XStyle.class),PageStyles.getByName("Standard"));
		}
		catch (Exception e) {}

		XPropertySet PropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, StdStyle);

		// changing/getting some properties
		XText HeaderText = null;

		try {
			PropSet.setPropertyValue("HeaderIsOn", new Boolean(true));
			PropSet.setPropertyValue("FooterIsOn", new Boolean(true));			
			
			HeaderText = (XText) UnoRuntime.queryInterface(XText.class, PropSet.getPropertyValue("HeaderText"));
			XTextCursor xTextCursor = (XTextCursor) _xaDoc.getText().createTextCursor();
			HeaderText.setString(_TheHeader);
		}
		catch (Exception e) {
			e.printStackTrace();
		} 
		
		
	}
	
    private void run() {
        try {
            if(SvrInfo.InitConnection()) {
                System.out.println("Connection successful !");
                xCC = SvrInfo.getCompCtx();
                xMCF = xCC.getServiceManager();
            //try to get a document
//                Object desktop = SvrInfo.getFactory().createInstanceWithContext("com.sun.star.frame.Desktop", xCC);
            // get the remote service manager
            // query its XDesktop interface, we need the current component
                //create an empty writer docum
                
    			//create a writer empty document
    			Object oDesktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", xCC);
    			//insert a title, H1
    			XComponentLoader aCompLd = (XComponentLoader)UnoRuntime.queryInterface(XComponentLoader.class, oDesktop);

    			// define load properties according to com.sun.star.document.MediaDescriptor

    			/* or simply create an empty array of com.sun.star.beans.PropertyValue structs:
    			    PropertyValue[] loadProps = new PropertyValue[0]
    			 */

    			// the boolean property Hidden tells the office to open a file in hidden mode
    			PropertyValue[] loadProps = new PropertyValue[2];
    			loadProps[0] = new PropertyValue();
    			loadProps[0].Name = "DocumentTitle";
    			loadProps[0].Value = new String("Certificate report"); 
    			loadProps[1] = new PropertyValue();
    			loadProps[1].Name = "Author";
    			loadProps[1].Value = new String("OXSIT signature extension"); 

    			// load
    			XComponent aDocComp = aCompLd.loadComponentFromURL("private:factory/swriter", "_blank", 0, loadProps); 

    			XTextDocument aTextDocument = (com.sun.star.text.XTextDocument) UnoRuntime.queryInterface(XTextDocument.class, aDocComp);
    			
    		     // get the XModel interface from the component
    		     XModel xModel = (XModel)UnoRuntime.queryInterface(XModel.class, aDocComp);
    		     
    		     // the model knows its controller
    		     XController xController = xModel.getCurrentController();
    		     
    		     // the controller gives us the TextViewCursor
    		     // query the viewcursor supplier interface 
    		     XTextViewCursorSupplier xViewCursorSupplier = 
    		         (XTextViewCursorSupplier)UnoRuntime.queryInterface(
    		             XTextViewCursorSupplier.class, xController);
    		     
    		     // get the cursor 
    		     XTextViewCursor xViewCursor = xViewCursorSupplier.getViewCursor();
    			

    		     setCustomPageStyle(aTextDocument);
    		     
    			//General certificate section H1
    			//insert a title, Heading level 1
    			insertAHeading(xViewCursor,"Certificate Report","Heading 1");

/*    			XPagePrintable xp = (XPagePrintable)UnoRuntime.queryInterface(XPagePrintable.class, aTextDocument);
    			
    			PropertyValue[] xps = xp.getPagePrintSettings();
    			
    			for(int i = 0; i < xps.length;i++) {
    				if(xps[i].Name.equals("IsLandscape")) {
    					xps[i].Value = new Boolean(true);
    					break;
    				}
    				trace(xps[i].Name);
    			}
    			
    			xp.setPagePrintSettings(xps);
    			
*/    			
    			//compute all the extensions + 11 other elements
    			
    			insertTable(aTextDocument, xViewCursor, 30, 4);
    			
    			xViewCursor.collapseToEnd();
    			xViewCursor.setString("Another string"+"\r"+"\r");
    			xViewCursor.collapseToEnd();
    			insertTable(aTextDocument, xViewCursor, 10, 4);
    			
    			
    			
    	        xModel = (XModel)UnoRuntime.queryInterface(XModel.class, aTextDocument);

    	        xController = xModel.getCurrentController();
    	        // the controller gives us the TextViewCursor
    			
    	        xViewCursor.gotoEnd(false);

    			//core certificate element H2

    			
    			//table with element, 3 columns: name, value, notes

    			//certificate critical extensions H2

    			//table with element, 3 columns: name, value, notes

    			//Not certificate critical extensions H2

    			//table with element, 3 columns: name, value, notes


    			//insert a Header
    			prepareAHeader(aTextDocument, "Certificate Report\n\n");
                

            }
        }
        catch(Exception e) {
            System.out.println("WARNING: exception thrown !\nJob aborted:\n"+e.toString());
        } catch (java.lang.Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
 
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestWriterReportCreation t = new TestWriterReportCreation();		
		t.run();
		System.exit(0);
	}	
}
