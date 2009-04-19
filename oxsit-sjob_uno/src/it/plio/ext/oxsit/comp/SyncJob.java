/*************************************************************************
 * 
 *  Copyright 2009 by Giuseppe Castagno beppec56@openoffice.org
 *  
 *  The Contents of this file are made available subject to
 *  the terms of European Union Public License (EUPL) version 1.1
 *  as published by the European Community.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the EUPL.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  EUPL for more details.
 *
 *  You should have received a copy of the EUPL along with this
 *  program.  If not, see:
 *  https://www.osor.eu/eupl, http://ec.europa.eu/idabc/eupl.
 *
 ************************************************************************/

package it.plio.ext.oxsit.comp;

import it.plio.ext.oxsit.Utilities;
import it.plio.ext.oxsit.comp.SingletonGlobalVarConstants;
import it.plio.ext.oxsit.comp.SingletonGlobalVariables;
import it.plio.ext.oxsit.logging.XDynamicLogger;
import it.plio.ext.oxsit.ooo.GlobConstant;
import it.plio.ext.oxsit.ooo.GlobalVariables;
import it.plio.ext.oxsit.security.cert.XOX_CertificateExtension;
import it.plio.ext.oxsit.security.cert.XOX_DocumentSignatures;
import it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate;
import it.plio.ext.oxsit.ooo.interceptor.DispatchInterceptor;
import it.plio.ext.oxsit.ooo.pack.DigitalSignatureHelper;

import com.sun.star.beans.NamedValue;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertyAccess;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XNameAccess;
import com.sun.star.container.XNameContainer;
import com.sun.star.document.XStorageBasedDocument;
import com.sun.star.drawing.XDrawPagesSupplier;
import com.sun.star.embed.XStorage;
import com.sun.star.frame.XController;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lib.uno.helper.ComponentBase;
import com.sun.star.sheet.XSpreadsheetDocument;
import com.sun.star.task.XJob;
import com.sun.star.text.XTextDocument;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.CloseVetoException;
import com.sun.star.util.XChangesBatch;
import com.sun.star.util.XCloseListener;
import com.sun.star.util.XCloseable;

/**
 * this class is the class to be registered when installing the extension
 * 
 * @author beppe
 * 
 */
public class SyncJob extends ComponentBase
	implements XServiceInfo, // general
		XJob, // synchronous Job interface (activates a Java thread for
		// the XDispatcherInterceptor operations)
		XCloseable {

	// needed for registration
	public static final String			m_sImplementationName	= SyncJob.class.getName();
	public static final String[]		m_sServiceNames			= { "com.sun.star.task.Job" };

	private XFrame						m_xFrame;												// use
	// when frame is needed as reference

	private XComponentContext			m_xComponentContext;

	@SuppressWarnings("unused")
	// may be we will need it afterward...
	private XMultiServiceFactory		m_xFactory				= null;

	// protected XMultiComponentFactory m_xRemoteServiceManager = null;
	protected XMultiComponentFactory	m_xServiceManager		= null;
	
	private final String sSingletonService = GlobConstant.m_sSINGLETON_LOGGER_SERVICE_INSTANCE;
	private SingletonGlobalVariables globalSign_data = null;
	private Object m_oSingleVarObj;	
	private XPropertyAccess m_aSingletonGlobVarProps;
	
	private	XDynamicLogger							m_logger;

	private Object m_oSingleLogObj;	
	
	/**
	 * The toolkit, that we can create UNO dialogs.
	 */
	/*
	 * private static XToolkit m_xToolkit = null;
	 */
	// next 3 static vars are for debug only
	// public static int m_nOnLoadCount = 0;
	// public static int m_nOnSaveCount = 0;
	// public static int m_nOnSaveAsCount = 0;
	/**
	 * Constructs a new instance
	 * 
	 * @param context
	 *            the XComponentContext
	 */
	public SyncJob(XComponentContext context) {

		m_oSingleLogObj = context.getValueByName(GlobConstant.m_sSINGLETON_LOGGER_SERVICE_INSTANCE);
		if(m_oSingleLogObj == null)
			System.out.println("cannot build first singleton logger!");
			
// the singleton is the first element that need to be build		
		m_oSingleVarObj = context.getValueByName(GlobConstant.m_sSINGLETON_SERVICE_INSTANCE);
		m_aSingletonGlobVarProps = (XPropertyAccess)UnoRuntime.queryInterface(XPropertyAccess.class, m_oSingleVarObj);
		m_logger = new XDynamicLogger(this,context);
		m_logger.enableLogging(); // comment this if no logging needed

		m_logger.ctor();
		m_xComponentContext = context;

		/*
		 * if(m_xComponentContext != null) System.out.println(" got a context!");
		 */
		try {
			// get the service manager from the component context
			m_xServiceManager = m_xComponentContext.getServiceManager();
		} catch (java.lang.Exception ex) {
			m_logger.severe("ctor", "No service manager!", ex);
		}

		m_xFactory = (XMultiServiceFactory)UnoRuntime.queryInterface(XMultiServiceFactory.class, m_xComponentContext);
	}

	/*
	 * public static XToolkit getToolkit() { return m_xToolkit; }
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.lang.XServiceInfo#getImplementationName()
	 */
	public String getImplementationName() {
		return m_sImplementationName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.lang.XServiceInfo#getSupportedServiceNames()
	 */
	public String[] getSupportedServiceNames() {
		return m_sServiceNames;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.lang.XServiceInfo#supportsService(java.lang.String)
	 */
	public boolean supportsService(String _sService) {
		int len = m_sServiceNames.length;

		for (int i = 0; i < len; i++) {
			if (_sService.equals( m_sServiceNames[i] ))
				return true;
		}
		return false;
	}

	/**
	 * Updates the Desktop current component in case of opening, creating or
	 * swapping to other document
	 * 
	 * @return XComponent Returns the current component of Desktop object
	 * 
	 */
	/*
	 * public void updateCurrentComponent(){
	 * 
	 * XComponent ret = null; Object desktop; try { desktop =
	 * m_xRemoteServiceManager.createInstanceWithContext("com.sun.star.frame.Desktop",
	 * m_xComponentContext); XDesktop xDesktop =
	 * (XDesktop)UnoRuntime.queryInterface(XDesktop.class, desktop); ret =
	 * xDesktop.getCurrentComponent();
	 * 
	 * this.m_xMultiComponentFactory = this.m_xContext.getServiceManager();
	 * this.m_xFactory = (XMultiServiceFactory)
	 * UnoRuntime.queryInterface(XMultiServiceFactory.class,
	 * this.m_xCurrentComponent); } catch (com.sun.star.uno.Exception ex) {
	 * ex.printStackTrace(); } this.m_xCurrentComponent = ret; }
	 */

	// /////////////////////////////////////////////////////////////////////////////////////
	/**
	 * implements the execute method of synch Job handler single method for XJob
	 * 
	 * @return the result of the job. The concrete semantics is
	 *         service-dependent. But it should be possible to - deregister the
	 *         job - let him registered although execution was successfully(!) -
	 *         make some job specific data persistent inside the job
	 *         configuration which is provided by the executor.
	 * 
	 * 
	 * as a test, implement once, when the OOo starts at the very beginning to
	 * initialize the stuff (change the elements)
	 */
	public Object execute(NamedValue[] lArgs) throws IllegalArgumentException, Exception {
		// TODO Auto-generated method stub
//		println( "execute() called !" );
		m_logger.info("execute", "");
		// detect the reason of calling this job
		com.sun.star.beans.NamedValue[] lGenericConfig = null;
		com.sun.star.beans.NamedValue[] lJobConfig = null;
		com.sun.star.beans.NamedValue[] lEnvironment = null;
		com.sun.star.beans.NamedValue[] lDynamicData = null;

		try {
			Object oObj = m_xComponentContext.getValueByName(sSingletonService);
			if(oObj != null)
				m_logger.info("execute"," singleton data "+String.format( "%8H", oObj.hashCode() ));
			else
				m_logger.info("execute","No singleton data (UNO)");
		}
		catch (ClassCastException e) {
			e.printStackTrace();
		}
		try {
			///////// try to get a Document Signatures object
			Object oObj = null;			
			
            Object args[]=new Object[2];
            args[0] = "arg1"; //here the first arg the URl, may be)
            args[1] = "arg2"; // the second one, top XStorage?, need to test it

			oObj = m_xServiceManager.createInstanceWithArgumentsAndContext(GlobConstant.m_sDOCUMENT_SIGNATURES_SERVICE, args, m_xComponentContext);

			if(oObj != null) {
//				Utilities.showInterfaces(oObj, oObj);
				m_logger.info("execute"," document signatures service exists"+String.format( "%8H", oObj.hashCode() ) );
				XNameContainer xName = (XNameContainer)UnoRuntime.queryInterface(XNameContainer.class, oObj);

				if(xName != null)
					xName.hasElements();
				else
					m_logger.info("execute"," document signatures service "+String.format( "%8H", oObj.hashCode() )+ " no XNameContainer" );
				
				XOX_DocumentSignatures xoxD = (XOX_DocumentSignatures)UnoRuntime.queryInterface(XOX_DocumentSignatures.class, oObj);
				if(xoxD != null)
					xoxD.getDocumentURL();
				else
					m_logger.info("execute"," document signatures service "+String.format( "%8H", oObj.hashCode() )+ " no XOXDocumentSignatures" );

				XOX_CertificateExtension xoxCE = (XOX_CertificateExtension)UnoRuntime.queryInterface(XOX_CertificateExtension.class, oObj);
				if(xoxCE != null)
					xoxCE.getExtensionId();
				else
					m_logger.info("execute"," document signatures service "+String.format( "%8H", oObj.hashCode() )+ " no XOX_CertificateExtension" );
				
				XOX_QualifiedCertificate xoxQC = (XOX_QualifiedCertificate)UnoRuntime.queryInterface(XOX_QualifiedCertificate.class, oObj);
				if(xoxQC != null)
					xoxQC.getVersion();
				else
					m_logger.info("execute"," document signatures service "+String.format( "%8H", oObj.hashCode() )+ " no XOX_QualifiedCertificate" );
				
				
			}
			else
				m_logger.info("execute","No document signatures service (UNO)");
		}
		catch (ClassCastException e) {
			e.printStackTrace();
		}

		int c = lArgs.length;
		for (int i = 0; i < c; ++i) {
			// System.out.println("Argument: "+lArgs[i].Name);
			if (lArgs[i].Name.equals( "Config" )) {
				lGenericConfig = (com.sun.star.beans.NamedValue[]) com.sun.star.uno.AnyConverter
						.toArray( lArgs[i].Value );
//				println("Config");
			}
			else if (lArgs[i].Name.equals( "JobConfig" )) {
				lJobConfig = (com.sun.star.beans.NamedValue[]) com.sun.star.uno.AnyConverter
						.toArray( lArgs[i].Value );
//				println("JobConfig");
			}
			else if (lArgs[i].Name.equals( "Environment" )) {
				lEnvironment = (com.sun.star.beans.NamedValue[]) com.sun.star.uno.AnyConverter
						.toArray( lArgs[i].Value );
//				println("Environment");
			}
			else if (lArgs[i].Name.equals( "DynamicData" )) {
				lDynamicData = (com.sun.star.beans.NamedValue[]) com.sun.star.uno.AnyConverter
						.toArray( lArgs[i].Value );
//				println("DynamicData");
			}
		}

		/*
		 * if (lGenericConfig == null) System.out.println("lGenericConfig is
		 * empty"); if (lJobConfig == null) System.out.println("lJobConfig is
		 * empty"); if (lDynamicData == null) System.out.println("lDynamicData
		 * is empty");
		 */
		// Analyze the environment info. This sub list is the only guaranteed
		// one!
		if (lEnvironment == null)
			m_logger.info( "lEnvironment is empty" );
		else {
			java.lang.String sEnvType = null;
			java.lang.String sEventName = null;
			com.sun.star.frame.XFrame xFrame = null;
			com.sun.star.frame.XModel xModel = null;
			c = lEnvironment.length;
			/*
			 * for (int i=0; i<c; ++i) {
			 * System.out.println(lEnvironment[i].Name); }
			 */
			for (int i = 0; i < c; ++i) {
				if (lEnvironment[i].Name.equals( "EnvType" )) {
					sEnvType = com.sun.star.uno.AnyConverter
							.toString( lEnvironment[i].Value );
				} else if (lEnvironment[i].Name.equals( "EventName" )) {
					sEventName = com.sun.star.uno.AnyConverter
							.toString( lEnvironment[i].Value );
				} else if (lEnvironment[i].Name.equals( "Frame" )) {

					xFrame = (com.sun.star.frame.XFrame) com.sun.star.uno.AnyConverter
							.toObject( new com.sun.star.uno.Type(
									com.sun.star.frame.XFrame.class ),
									lEnvironment[i].Value );
					/*
					 * System.out.println("frame received "); m_xFrame = xFrame;
					 * showMessageBox("event received", sEventName); m_xFrame =
					 * null;
					 */
				} else if (lEnvironment[i].Name.equals( "Model" )) {
					xModel = (com.sun.star.frame.XModel) com.sun.star.uno.AnyConverter
							.toObject( new com.sun.star.uno.Type(
									com.sun.star.frame.XModel.class ),
									lEnvironment[i].Value );
				}
			}
			if (xModel != null) {
				XController xController = xModel.getCurrentController();
				if (xController != null) {
					xFrame = xController.getFrame();
				}
			}
			/**
			 * environment analized, so, do as requested
			 * OOo code dealing with event firing is in
			 * sfx2/source/appl/appinit.cxx#306 (dev300-m10)
			 */
			if (sEventName != null) {
				m_logger.info("execute", "event received: " + sEventName );

				GlobalVariables test = GlobalVariables.getInstance();
				test.logSomething(this.toString()+ " "+sEventName);
				
				if(globalSign_data == null)
					m_logger.info("execute", "No singleton data (Java)");
				
				// if (sEventName.equalsIgnoreCase( "onFirstVisibleTask" )) {
				if (sEventName.equalsIgnoreCase( "OnStartApp" )
						/*|| sEventName.equalsIgnoreCase( "OnCloseApp" )*/) {

					GlobalVariables test2 = GlobalVariables.getInstance();
					test2.logSomething(this.toString()+ " "+sEventName);
					
//					UniqueGlobalData aData;

//					cleanupAllFrameData();
				} else if (sEventName.equalsIgnoreCase( "OnViewCreated" )) {
///////////////////// OnViewCreated //////////////////////////7
					/**
					 * this event is fired at the end of the method sal_Bool
					 * SfxTopFrame::InsertDocument( SfxObjectShell* pDoc ) in
					 * file sfx2/source/view/topfrm.cxx When the view has been
					 * created and the model, frame, controller are all
					 * connected
					 * 
					 * try to get the frame, then activate at the frame our
					 * interceptor
					 * 
					 */
					if (xFrame != null) {
						m_xFrame = xFrame;
						xModel = m_xFrame.getController().getModel();
						// get the component and the model, so the interceptor
						// will have all done
						// detect if the document we are linked to is a Writer,
						// an Impress or a Draw one
						// if false the CNIPA signature is not supported
						// get the component
						// we query the interface XSpreadsheetDocument from the
						// model
						XSpreadsheetDocument xSpreadsheetDocument = (XSpreadsheetDocument) UnoRuntime
								.queryInterface( XSpreadsheetDocument.class, xModel );
						// we query the interface XTextDocument from the model
						XTextDocument xTextDocument = (XTextDocument) UnoRuntime
								.queryInterface( XTextDocument.class, xModel );
						// we query the interface XDrawPagesSupplier from the
						// model
						XDrawPagesSupplier xDrawDocument = (XDrawPagesSupplier) UnoRuntime
								.queryInterface( XDrawPagesSupplier.class, xModel );

						if (xSpreadsheetDocument != null || xTextDocument != null
								|| xDrawDocument != null) {
							// a CNIPA signable type of document, then start the
							// interceptor
							DispatchInterceptor aInterceptor = new DispatchInterceptor(
									xFrame, m_xComponentContext, m_xServiceManager );
							aInterceptor.startListening();
							m_logger.info("execute", "DispatchInterceptor started");
						}
					}
				} else if (sEventName.equalsIgnoreCase( "OnLoad" )) {
					/**
					 * this event is fired when the document loading is finished
					 * 
					 * seems in sfx2/source/doc/objstor.cxx#577 (dev300-m10)
					 * though I'm not sure
					 * 
					 */

					if (xFrame != null) {
						m_xFrame = xFrame;
						m_logger.info("execute", "document loaded URL: " + xModel.getURL() );
						
// grab the XStorage interface of this document
/*
 * unfortunately, the storage we get this way doesn't give us access to the
 * whole stuff
 * some part of the storage throw an IOError exception, something that doesn't happen
 * if the package is opened anew using the URL
 */
						XStorage xStorage = null;
						XStorageBasedDocument xDocStorage =
									(XStorageBasedDocument)UnoRuntime.queryInterface( XStorageBasedDocument.class, xModel );

						xStorage = xDocStorage.getDocumentStorage();
						if(xStorage != null) {
							m_logger.info("execute"+" We have storage available!");
							Utilities.showInterfaces(xModel, xStorage);
						}
						/**
						 * we can read the file and check if a CNIPA signature
						 * TODO start a thread to do the job? may be creating a
						 * sort of user feedback if the file is big or composed
						 * of a lot of images
						 * 
						 */
						// check the signature status and init the proper value
						// in the configuration, check the signature and set the
						// status properly
						// after the creation the frame data are manipulated by the
						// toolbar dispatcher.
						// if the dispatcher is dead, then no longer needs them,
						// then the data will be cleared at the next app start
//						initThisFrameData( xModel.getURL(), true );
						
//new methos						
						initThisDocumentURLData(xStorage, xModel.getURL(), true, xFrame);
					}
				}/* else if (sEventName.equalsIgnoreCase( "OnUnload" )) {
					// delete the single frame data
					if (xFrame != null) {
						m_xFrame = xFrame;
						String aUrl = xModel.getURL();
						if (aUrl.length() > 0) {
							removeThisFrameData( aUrl );
						}
					}
				}*/ else if (sEventName.equalsIgnoreCase( "OnSaveDone" )) {
					// only clear the status of the corresponding frame
					if (xFrame != null) {
						m_xFrame = xFrame;
						String aUrl = xModel.getURL();
						if (aUrl.length() > 0) {
							clearStatusOfThisFrame( aUrl );
						}
					}
				} else if (sEventName.equalsIgnoreCase( "OnSaveAsDone" )) {
					if (xFrame != null) {
						m_xFrame = xFrame;
						String aUrl = xModel.getURL();
						if (aUrl.length() > 0) {
							// no signature check
//							initThisFrameData( xModel.getURL(), false );
							//new methos
							// FIXME may be we need to swap the old URL to the new one, besides
							// resetting the signature status to 'non available'
							initThisDocumentURLData(null, xModel.getURL(), false, xFrame);
						}
					}
				}
			}
		}
		return null;
	}

	/*
	 * this section contains all the method needed to access the registry. It
	 * seems that the Sync job MUST have the registry manipulation inside
	 * itself. There isn't the possibility to do it in an another java class
	 * even inside the same package
	 * 
	 */

	/**
	 * 
	 * 
	 * @param _TheViewRoot
	 */
	private void commitChanges(Object _TheViewRoot) {
		XChangesBatch xUpdateControl = (XChangesBatch) UnoRuntime.queryInterface(
				XChangesBatch.class, _TheViewRoot );
		try {
			xUpdateControl.commitChanges();
		} catch (WrappedTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void clearStatusOfThisFrame(String aURL) {
		// get provider, open view, grab the url
		String aURLHash = Utilities.getFrameHash( aURL );

		// create a default frame structure, if existent do nothing
		// get provider
		XMultiServiceFactory aConfProvider = getConfigurationProvider();

		// create a read/write view at the root frame configuration level
//		println( "2" );
		// Object xViewRoot = getReadWriteView( aConfProvider,
		// it.plio.ext.cnipa.signature.GlobConstant.m_sExtensionConfFrameKey+
		// aURLHash);
		Object xViewRoot = getReadWriteView( aConfProvider,
				GlobConstant.m_sEXTENSION_CONF_FRAME_KEY );
//		println( "3" );
		if (xViewRoot != null) {
			// try to see if the frame is already available, it should be...
			// grab note container
			XNameAccess xNAccess = (XNameAccess) UnoRuntime.queryInterface(
					XNameAccess.class, xViewRoot );

			Object oObj = null;
			try {
				oObj = xNAccess.getByName( aURLHash );
			} catch (NoSuchElementException e1) {
				// TODO Auto-generated catch block
				m_logger.severe("execute", "No element", e1);
				e1.printStackTrace();
			} catch (WrappedTargetException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			m_logger.info( "oObj "+ oObj );

			// XPropertySet xPS = (XPropertySet) UnoRuntime.queryInterface(
			// XPropertySet.class, xViewRoot );
			XPropertySet xPS = (XPropertySet) UnoRuntime.queryInterface(
					XPropertySet.class, oObj );
//			passert( "xPS", xPS );
			// Utilities.showProperties( xPS );
			// set the SignatureStatus property (defined in
			// AddConfiguration.xcs.xml)
			// to the parameter value get the value
//			println( "4" );
			if (xPS != null) {
				try {
					// xPS.setPropertyValue( "SignatureStatus", new Integer(
					// it.plio.ext.cnipa.signature.GlobConstant.SIGNATURESTATE_NOSIGNATURES)
					// );
					xPS
							.setPropertyValue(
									"SignatureStatus",
									new Integer(
											GlobConstant.m_nSIGNATURESTATE_NOSIGNATURES ) );
					m_logger.info( "status set to 0" );
				} catch (UnknownPropertyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (PropertyVetoException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (WrappedTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else
				m_logger.info( "no property" );
			m_logger.info( "5" );
			commitChanges( xViewRoot );
			// close the view
			( (XComponent) UnoRuntime.queryInterface( XComponent.class, xViewRoot ) )
					.dispose();
		} else
			m_logger.info( "no view: reinstall the extension " );
	}

	private void removeThisFrameData(String aURL) {
		String aURLHash = Utilities.getFrameHash( aURL );

		if (m_oSingleVarObj != null) {
			XNameContainer xNCont = (XNameContainer) UnoRuntime.queryInterface(
					XNameContainer.class, m_oSingleVarObj );

			// if exists, remove the aURL object
			if (xNCont.hasByName( aURLHash )) {
				try {
					// remove the node
					xNCont.removeByName( aURLHash );
					m_logger.info( "removed: " + aURLHash);
				} catch (NoSuchElementException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (WrappedTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * this method initialize the document data in the registry. if this is the
	 * first time the signature is checked
	 * @param _xStorage TODO
	 * @param aURL
	 * @param frame the frame this document belongs to, or null if not frame
	 */
	private void initThisDocumentURLData(XStorage _xStorage, String aURL, boolean _bCheckSignature, XFrame frame) {
		String aURLHash = Utilities.getFrameHash( aURL );

		// first remove this frame data, just to be sure, the document is
		// loaded only once, other windows won't trigger another OnLoad event on
		// the same document
		// removeThisFrameData( aURL );

		// create a read/write view at the root frame configuration level
		if (m_oSingleVarObj != null) {
			// search the hash
			XNameContainer xNCont = (XNameContainer) UnoRuntime.queryInterface(
					XNameContainer.class, m_oSingleVarObj );
			// if exist, set value to zero, else create it at default value
			if (!xNCont.hasByName( aURLHash )) {
				m_logger.info( "adding name: " + aURLHash );
				// frame doesn't exist, first create it
				// doesn't exist, so add it to the current view, provide default
				// value for status and exits
				// need to add a named container to hold the frame name
				
				// so create and empty record, as expected by the object
				
				PropertyValue[] aValues = new PropertyValue[3];
				
				aValues[0] = new PropertyValue();
				aValues[0].Name = new String(SingletonGlobalVarConstants.m_sPROPERTY_OPERATION);
				aValues[0].Value = new Integer(SingletonGlobalVarConstants.m_nADD_PROPERTY);
					
				aValues[1] = new PropertyValue();
				aValues[1].Name = new String(SingletonGlobalVarConstants.m_sURL_VALUE);
				aValues[1].Value = new String(aURL);

				aValues[2] = new PropertyValue();
				aValues[2].Name = new String(SingletonGlobalVarConstants.m_sXADES_SIGNATURE_STATE);
				aValues[2].Value = new Integer(GlobConstant.m_nSIGNATURESTATE_NOSIGNATURES);
				PropertyValue aParVal = new PropertyValue();
				aParVal.Name = new String(aURLHash);
				aParVal.Value = aValues;

				// insert it - this also names the element
				try {
					xNCont.insertByName( aURLHash, aParVal );
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// read again to see if now it exists
			if (xNCont.hasByName( aURLHash )) {
				// now update the status of the property
				m_logger.info( "frame data root added successfully " );

				int _nSignState = GlobConstant.m_nSIGNATURESTATE_NOSIGNATURES;

				if (_bCheckSignature) {
					// ///////////////////////////////////////////////
					// FIXME (01) we need to add here the signature check!
					// ////////////////////////////////////////////////
					/*
					 * arrive here with:
					 */
//					try {
					m_logger.info( "simulating signature check..." );
//						Thread.sleep( 20 ); //simulates the time needed to check signatures
						DigitalSignatureHelper aCls = new DigitalSignatureHelper(m_xServiceManager, m_xComponentContext);
//						aCls.examinePackageODT(aURL, m_xServiceManager, m_xComponentContext);
						aCls.verifyDocumentSignature(_xStorage, aURL);
/*					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}*/

					// FIXME (02) this statement needs to be changed into the right one according to signature check result!!
//					_nSignState = it.plio.ext.cnipa.signature.GlobConstant.SIGNATURESTATE_NOSIGNATURES;
//					_nSignState = it.plio.ext.cnipa.signature.GlobConstant.SIGNATURESTATE_SIGNATURES_NOTVALIDATED;
				}

// simply udate the signature state of our element
				try {
					PropertyValue[] aValues = new PropertyValue[2];
					aValues[0] = new PropertyValue();
					aValues[0].Name = new String(SingletonGlobalVarConstants.m_sPROPERTY_OPERATION);
					aValues[0].Value = new Integer(SingletonGlobalVarConstants.m_nSET_PROPERTY);
					aValues[1] = new PropertyValue();
					aValues[1].Name = new String(SingletonGlobalVarConstants.m_sXADES_SIGNATURE_STATE);
					aValues[1].Value = new Integer(_nSignState);

					PropertyValue[] aParVal = new PropertyValue[1];
					aParVal[0].Name = new String(aURLHash);
					aParVal[0].Value = aValues;
					m_aSingletonGlobVarProps.setPropertyValues(aParVal);
				} catch (UnknownPropertyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (PropertyVetoException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (WrappedTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else
				m_logger.info( "problems creating new frame data on Singleton var" );
		} else
			m_logger.info( "the main Singleton Var object is missing!" );	
	}

	private XMultiServiceFactory getConfigurationProvider() {
		XMultiServiceFactory aConfProvider = null;
		// get provider
		final String sProviderService = "com.sun.star.configuration.ConfigurationProvider";

		try {
			aConfProvider = (XMultiServiceFactory) UnoRuntime.queryInterface(
					XMultiServiceFactory.class, m_xServiceManager
							.createInstanceWithContext( sProviderService, m_xComponentContext ) );
		} catch (Exception e) {
			e.printStackTrace();
			m_logger.info( "error !" );
		}
		return aConfProvider;
	}

	/**
	 * return an opened read write view on a configuration node
	 * 
	 * @param aConfProvider
	 * @param aPath
	 * @return
	 */
	private Object getReadWriteView(XMultiServiceFactory aConfProvider, String aPath) {
		// The service name: Need update access:
		final String cUpdatableView = "com.sun.star.configuration.ConfigurationUpdateAccess";

		// creation arguments: nodepath
		com.sun.star.beans.PropertyValue aPathArgument = new com.sun.star.beans.PropertyValue();
		aPathArgument.Name = "nodepath";
		aPathArgument.Value = GlobConstant.m_sEXTENSION_CONF_FRAME_KEY;
		// creation arguments: commit mode - write-through or write-back
		com.sun.star.beans.PropertyValue aModeArgument = new com.sun.star.beans.PropertyValue();
		aModeArgument.Name = "enableasync";
		aModeArgument.Value = new Boolean( false );

		Object[] aArguments = new Object[2];
		aArguments[0] = aPathArgument;
		aArguments[1] = aModeArgument;

		// create the view
		Object xViewRoot = null;
		try {
			xViewRoot = aConfProvider.createInstanceWithArguments( cUpdatableView,
					aArguments );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return xViewRoot;
	}

	// remove the frame data structure from our private configuration
	private void cleanupAllFrameData() {
		// get provider
		XMultiServiceFactory aConfProvider = getConfigurationProvider();
		// create a read/write view
		// remove all the frame data
		Object xViewRoot = getReadWriteView( aConfProvider,
				GlobConstant.m_sEXTENSION_CONF_FRAME_KEY );
		if (xViewRoot != null) {
			XNameContainer xNCont = (XNameContainer) UnoRuntime.queryInterface(
					XNameContainer.class, xViewRoot );
			String[] elementsToRemove = xNCont.getElementNames();
			if (elementsToRemove.length > 0) {
				for (int i = 0; i < elementsToRemove.length; i++) {
					m_logger.info( "removing: " + elementsToRemove[i] + ", " );

					try {
						xNCont.removeByName( elementsToRemove[i] );
					} catch (NoSuchElementException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (WrappedTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				m_logger.info( "" );
			}
			commitChanges( xViewRoot );
			// close the view
			( (XComponent) UnoRuntime.queryInterface( XComponent.class, xViewRoot ) )
					.dispose();
		}
	}

	// remove the frame data structure from our private configuration
	private void cleanupAllDocumentURLData() {
		GlobalVariables globalVar = GlobalVariables.getInstance();
		globalVar.removeAllDocumentURL();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.util.XCloseable#close(boolean)
	 */
	public void close(boolean arg0) throws CloseVetoException {
		// TODO Auto-generated method stub
		m_logger.info( "close called" );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.util.XCloseBroadcaster#addCloseListener(com.sun.star.util.XCloseListener)
	 */
	public void addCloseListener(XCloseListener arg0) {
		// TODO Auto-generated method stub
		m_logger.info( "addCloseListener called" );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.util.XCloseBroadcaster#removeCloseListener(com.sun.star.util.XCloseListener)
	 */
	public void removeCloseListener(XCloseListener arg0) {
		// TODO Auto-generated method stub
		m_logger.info( "removeCloseListener called" );
	}

}
