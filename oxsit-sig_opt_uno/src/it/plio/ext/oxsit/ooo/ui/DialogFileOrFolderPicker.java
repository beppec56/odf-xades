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

package it.plio.ext.oxsit.ooo.ui;

import it.plio.ext.oxsit.Utilities;

import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XInitialization;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.ui.dialogs.XExecutableDialog;
import com.sun.star.ui.dialogs.XFilePicker;
import com.sun.star.ui.dialogs.XFilePickerControlAccess;
import com.sun.star.ui.dialogs.XFilterManager;
import com.sun.star.ui.dialogs.XFolderPicker;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * a wrapper for file / flder functions:
 * open a file, save a file, choose a folder
 * @author beppec56
 *
 */
public class DialogFileOrFolderPicker {

	XMultiComponentFactory m_xMCF;
	XComponentContext m_xContext;

	public DialogFileOrFolderPicker(XMultiComponentFactory _xMCF, XComponentContext _Ctx) {
		m_xMCF = _xMCF;
		m_xContext = _Ctx;
	}

	/** raises a folderpicker in which the user can browse and select a path
	   * @param _sDisplayDirectory the path to the directory that is initially displayed
	   * @param _sTitle the title of the folderpicker
	   * @return the path to the folder that the user has selected. if the user has closed 
	   * the folderpicker by clicking the "Cancel" button
	   * an empty string is returned
	   * @see com.sun.star.ui.dialogs.FolderPicker
	   */
	  public String runFolderPicker( String _sTitle, String _sDisplayDirectory) {
	  String sReturnFolder = "";
	  XComponent xComponent = null;
	  try {
	      // instantiate the folder picker and retrieve the necessary interfaces...
	      Object oFolderPicker = m_xMCF.createInstanceWithContext("com.sun.star.ui.dialogs.FolderPicker", m_xContext);
	      XFolderPicker xFolderPicker = (XFolderPicker) UnoRuntime.queryInterface(XFolderPicker.class, oFolderPicker);
	      XExecutableDialog xExecutable = (XExecutableDialog) UnoRuntime.queryInterface(XExecutableDialog.class, oFolderPicker);
	      xComponent = (XComponent) UnoRuntime.queryInterface(XComponent.class, oFolderPicker);
	      xFolderPicker.setDisplayDirectory(_sDisplayDirectory);
	      // set the dialog title...
	      xFolderPicker.setTitle(_sTitle);
	      // show the dialog...
	      short nResult = xExecutable.execute();
	 
	      // User has clicked "Select" button...
	      if (nResult == com.sun.star.ui.dialogs.ExecutableDialogResults.OK){
	          sReturnFolder = xFolderPicker.getDirectory();
	      }
	 
	  }catch( Exception exception ) {
	      exception.printStackTrace(System.out);
	  } 
	  finally{
	      //make sure always to dispose the component and free the memory!
	      if (xComponent != null){
	          xComponent.dispose();
	      }
	  }
	      // return the selected path. If the user has clicked cancel an empty string is 
	      return sReturnFolder;
	  }
	
	public String runSaveAsDialog() {
		String sStorePath = ""; 
		XComponent xComponent = null;
		try {
			// the filepicker is instantiated with the global Multicomponentfactory...
			Object oFilePicker = m_xMCF.createInstanceWithContext("com.sun.star.ui.dialogs.FilePicker", m_xContext);
			XFilePicker xFilePicker = (XFilePicker) UnoRuntime.queryInterface(XFilePicker.class, oFilePicker);

			// the defaul tname is the initially proposed filename..
			xFilePicker.setDefaultName("MyExampleDocument");

			// set the initial displaydirectory. In this example the user template directory is used
			Object oPathSettings = m_xMCF.createInstanceWithContext("com.sun.star.util.PathSettings",m_xContext); 
			XPropertySet xPropertySet = (XPropertySet) com.sun.star.uno.UnoRuntime.queryInterface(XPropertySet.class, oPathSettings); 
			String sTemplateUrl = (String) xPropertySet.getPropertyValue("Template_writable");
			xFilePicker.setDisplayDirectory(sTemplateUrl);

			// set the filters of the dialog. The filternames may be retrieved from
			// http://wiki.services.openoffice.org/wiki/Framework/Article/Filter
			XFilterManager xFilterManager = (XFilterManager) UnoRuntime.queryInterface(XFilterManager.class, xFilePicker);
			xFilterManager.appendFilter("OpenDocument Text Template", "writer8_template"); 
			xFilterManager.appendFilter("OpenDocument Text", "writer8");

			// choose the template that defines the capabilities of the filepicker dialog
			XInitialization xInitialize = (XInitialization) UnoRuntime.queryInterface(XInitialization.class, xFilePicker);
			Short[] listAny = new Short[] { new Short(com.sun.star.ui.dialogs.TemplateDescription.FILESAVE_AUTOEXTENSION)};
			xInitialize.initialize(listAny);

			// add a control to the dialog to add the extension automatically to the filename...
			XFilePickerControlAccess xFilePickerControlAccess = (XFilePickerControlAccess) UnoRuntime.queryInterface(XFilePickerControlAccess.class, xFilePicker);
			xFilePickerControlAccess.setValue(com.sun.star.ui.dialogs.ExtendedFilePickerElementIds.CHECKBOX_AUTOEXTENSION, (short) 0, new Boolean(true));

			xComponent = (XComponent) UnoRuntime.queryInterface(XComponent.class, xFilePicker);

			// execute the dialog...
			XExecutableDialog xExecutable = (XExecutableDialog) UnoRuntime.queryInterface(XExecutableDialog.class, xFilePicker);
			short nResult = xExecutable.execute();

			// query the resulting path of the dialog...
			if (nResult == com.sun.star.ui.dialogs.ExecutableDialogResults.OK){
				String[] sPathList = xFilePicker.getFiles();
				if (sPathList.length > 0){
					sStorePath = sPathList[0]; 
				}
			}

		} catch (com.sun.star.uno.Exception exception) {
			exception.printStackTrace();
		}
		finally{
			//make sure always to dispose the component and free the memory!
			if (xComponent != null){
				xComponent.dispose();
			}
		}

		return sStorePath;
	}

	/**
	 * 
	 * @param _sTitle dialog title
	 * @param _folderURL the last directory selected, or "" if nothing
	 * @param _fileName TODO
	 * @param _nTemplateDesc
	 * @return
	 */
	protected String runOpenFileDialogHelper(String _sTitle, String _folderURL, String _fileName, short _nTemplateDesc) {
		String sStorePath = ""; 
		XComponent xComponent = null;
		
		//form the start path from the start path of the _fileURL parameter

		try {
			// the filepicker is instantiated with the global Multicomponentfactory...
			Object oFilePicker = m_xMCF.createInstanceWithContext("com.sun.star.ui.dialogs.FilePicker", m_xContext);
			XFilePicker xFilePicker = (XFilePicker) UnoRuntime.queryInterface(XFilePicker.class, oFilePicker);

			// the default name is the initially proposed filename..
			xFilePicker.setDefaultName(_fileName);

			// set the initial displaydirectory. In this example the user template directory is used
			Object oPathSettings = m_xMCF.createInstanceWithContext("com.sun.star.util.PathSettings",m_xContext); 
			XPropertySet xPropertySet = (XPropertySet) com.sun.star.uno.UnoRuntime.queryInterface(XPropertySet.class, oPathSettings);
//			Utilities.showProperties(oPathSettings, xPropertySet);
			String sTemplateUrl;
			if(_folderURL.length() == 0)
				sTemplateUrl = (String) xPropertySet.getPropertyValue("Work_writable");
			else
				sTemplateUrl = _folderURL;
			xFilePicker.setDisplayDirectory(sTemplateUrl);
			xFilePicker.setTitle(_sTitle);

			// set the filters of the dialog. The filternames may be retrieved from
			// http://wiki.services.openoffice.org/wiki/Framework/Article/Filter
/*			XFilterManager xFilterManager = (XFilterManager) UnoRuntime.queryInterface(XFilterManager.class, xFilePicker);
			xFilterManager.appendFilter("OpenDocument Text Template", "writer8_template"); 
			xFilterManager.appendFilter("OpenDocument Text", "writer8");*/

			// choose the template that defines the capabilities of the filepicker dialog
			XInitialization xInitialize = (XInitialization) UnoRuntime.queryInterface(XInitialization.class, xFilePicker);
			Short[] listAny = new Short[] { new Short(_nTemplateDesc)};
			xInitialize.initialize(listAny);

			// add a control to the dialog to add the extension automatically to the filename...
/*			XFilePickerControlAccess xFilePickerControlAccess = (XFilePickerControlAccess) UnoRuntime.queryInterface(XFilePickerControlAccess.class, xFilePicker);
			xFilePickerControlAccess.setValue(com.sun.star.ui.dialogs.ExtendedFilePickerElementIds.CHECKBOX_AUTOEXTENSION, (short) 0, new Boolean(true));*/

			xComponent = (XComponent) UnoRuntime.queryInterface(XComponent.class, xFilePicker);

			// execute the dialog...
			XExecutableDialog xExecutable = (XExecutableDialog) UnoRuntime.queryInterface(XExecutableDialog.class, xFilePicker);
			short nResult = xExecutable.execute();

			// query the resulting path of the dialog...
			if (nResult == com.sun.star.ui.dialogs.ExecutableDialogResults.OK){
				String[] sPathList = xFilePicker.getFiles();
				if (sPathList.length > 0){
					sStorePath = sPathList[0]; 
				}
			}
		} catch (com.sun.star.uno.Exception exception) {
			exception.printStackTrace();
		}
		finally{
			//make sure always to dispose the component and free the memory!
			if (xComponent != null){
				xComponent.dispose();
			}
		}
		return sStorePath;
	}	
	
	public String runOpenFileDialog(String _sTitle) {
		return runOpenFileDialogHelper(_sTitle, "", "", com.sun.star.ui.dialogs.TemplateDescription.FILEOPEN_SIMPLE);
	}

	public String runOpenReadOnlyFileDialog(String _sTitle, String _folderURL, String _fileName) {		
		return runOpenFileDialogHelper(_sTitle, _folderURL, _fileName, com.sun.star.ui.dialogs.TemplateDescription.FILEOPEN_READONLY_VERSION);
	}
}
