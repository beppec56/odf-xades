/**
 * 
 */
package com.yacme.ext.oxsit.cust_it.comp.security.xades;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.nio.charset.Charset;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Represents an instance of signed doc in DIGIDOC format. Contains one or more
 * DataFile -s and zero or more Signature -s.
 * 
 * @author Veiko Sinivee
 * @version 1.0
 */
public class ODFXadesSignedDoc extends SignedDoc implements Serializable {
	/** digidoc format */
//	private String m_format;
	/** format version */
//	private String m_version;
	/** DataFile objects */
	private ArrayList m_dataFiles;
	/** Signature objects */
	private ArrayList m_signatures;



	/**
	 * Creates new SignedDoc
	 * 
	 * @param format
	 *            file format name
	 * @param version
	 *            file version number
	 * @throws SignedDocException
	 *             for validation errors
	 */
	public ODFXadesSignedDoc(String format, String version)
			throws SignedDocException {
		super(format, version);
		
	}


	/**
	 * Helper method to validate a format
	 * 
	 * @param str
	 *            input data
	 * @return exception or null for ok
	 */
	private SignedDocException validateFormat(String str) {
		SignedDocException ex = null;
		if (str == null || (!str.equals(FORMAT_ODF_XADES)))
			ex = new SignedDocException(SignedDocException.ERR_DIGIDOC_FORMAT,
					"Currently supports only ODF_XADES format", null);
		return ex;
	}






	/**
	 * Writes the SignedDoc to an output file and automatically calculates
	 * DataFile sizes and digests
	 * 
	 * @param outputFile
	 *            output file name
	 * @throws SignedDocException
	 *             for all errors
	 */
	public void writeToStream(OutputStream os) throws SignedDocException {
		// TODO read DataFile elements from old file

		try {
			os.write(xmlHeader().getBytes());
			//ROB: no xml output for ExternalDataFile
			/*
			for (int i = 0; i < countDataFiles(); i++) {
				DataFile df = getDataFile(i);
				df.writeToFile(os);
				os.write("\n".getBytes());
			}
			*/
			for (int i = 0; i < countSignatures(); i++) {
				Signature sig = getSignature(i);
				os.write(sig.toXML());
				os.write("\n".getBytes());
			}
			os.write(xmlTrailer().getBytes());
		} catch (SignedDocException ex) {
			throw ex; // already handled
		} catch (Exception ex) {
			SignedDocException.handleException(ex,
					SignedDocException.ERR_WRITE_FILE);
		}
	}
	
	//ROB: From uji
	public byte[] addODFData(ODFDocument odf) throws SignedDocException {
		
		byte[] manifestBytes = null;
		
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
				.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder;
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();

			// Acceso al manifest.xml y a la lista de elementos que contiene
			InputStream manifest = new ByteArrayInputStream(odf.getEntry("META-INF/manifest.xml"));

			Document docManifest = documentBuilder.parse(manifest);
			Element rootManifest = docManifest.getDocumentElement();
			NodeList listFileEntry = rootManifest
					.getElementsByTagName("manifest:file-entry");

			for (int i = 0; i < listFileEntry.getLength(); i++) {
				Element e = ((Element) listFileEntry.item(i));

				String fullPath = e.getAttribute("manifest:full-path");
				String mediaType = e.getAttribute("manifest:media-type");

				// Solo procesamos los ficheros
				if (!fullPath.endsWith("/")
						&& !fullPath.equals("META-INF/documentsignatures.xml")) {
					if ((odf.getEntry(fullPath).length != 0)
							&& (fullPath.equals("manifest.rdf") || fullPath
									.endsWith(".xml"))) {
						// Obtenemos el fichero, canonizamos y calculamos el
						// digest
						InputStream xmlFile = new ByteArrayInputStream(odf
								.getEntry(fullPath));

						ExternalDataFile df = new ExternalDataFile(xmlFile,
								fullPath, mediaType, fullPath,
								ExternalDataFile.CONTENT_ODF_PKG_XML_ENTRY,
								this);
						addDataFile(df);

					} else {

						InputStream binaryStream = new ByteArrayInputStream(odf
								.getEntry(fullPath));
						ExternalDataFile df = new ExternalDataFile(binaryStream,
								fullPath, mediaType, fullPath,
								ExternalDataFile.CONTENT_ODF_PKG_BINARY_ENTRY,
								this);
						addDataFile(df);

					}

				}
			}
			// ROB: mimetype
			if (odf.hasEntry("mimetype")) {

				InputStream xmlStream = new ByteArrayInputStream(odf
						.getEntry("mimetype"));
				ExternalDataFile df = new ExternalDataFile(xmlStream, "mimetype",
						"text/text", "mimetype",
						ExternalDataFile.CONTENT_ODF_PKG_BINARY_ENTRY, this);
				addDataFile(df);
			}

			// ROB creazione del data file per manifest.xml aggiornato
			// Añadimos el fichero de firma al manifest.xml
			// Aggiungiamo a manifest.xml l'entry per documensignatures.xml
			Element nodeDocumentSignatures = docManifest
					.createElement("manifest:file-entry");
			nodeDocumentSignatures.setAttribute("manifest:media-type", "");
			nodeDocumentSignatures.setAttribute("manifest:full-path",
					"META-INF/xadessignatures.xml");
			rootManifest.appendChild(nodeDocumentSignatures);

			Element nodeMetaInf = docManifest
					.createElement("manifest:file-entry");
			nodeMetaInf.setAttribute("manifest:media-type", "");
			nodeMetaInf.setAttribute("manifest:full-path", "META-INF/");
			rootManifest.appendChild(nodeMetaInf);
			
			ByteArrayOutputStream manifestOs = new ByteArrayOutputStream();
			writeXML(manifestOs, rootManifest, false);
			manifestBytes = manifestOs.toByteArray();
			ByteArrayInputStream manifestIs = new ByteArrayInputStream(manifestBytes);
			
			ExternalDataFile df = new ExternalDataFile(manifestIs, "META-INF/manifest.xml",
					"text/text", "META-INF/manifest.xml",
					ExternalDataFile.CONTENT_ODF_PKG_XML_ENTRY, this);
			addDataFile(df);
			
			
			

		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return manifestBytes;
	}


	/**
	 * return a new available Signature id
	 * 
	 * @return new Signature id
	 */
	public String getNewSignatureId() {
		String id = "ID_"+UUID.randomUUID().toString();
		return id;
	}

	/**
	 * Adds a new uncomplete signature to signed doc
	 * 
	 * @param cert
	 *            signers certificate
	 * @param claimedRoles
	 *            signers claimed roles
	 * @param adr
	 *            signers address
	 * @return new Signature object
	 */
	public Signature prepareSignature(X509Certificate cert,
			String[] claimedRoles, SignatureProductionPlace adr)
			throws SignedDocException {
		Signature sig = new Signature(this);
		sig.setId(getNewSignatureId());
		// create SignedInfo block
		SignedInfo si = new SignedInfo(sig, RSA_SHA1_SIGNATURE_METHOD,
				CANONICALIZATION_METHOD_20010315);
		// add DataFile references
		for (int i = 0; i < countDataFiles(); i++) {
			DataFile df = getDataFile(i);
			Reference ref = new Reference(si, df);
			ref.setUri(df.getId());
			si.addReference(ref);
		}
		// create key info
		KeyInfo ki = new KeyInfo(cert);
		sig.setKeyInfo(ki);
		ki.setSignature(sig);
		CertValue cval = new CertValue();
		cval.setType(CertValue.CERTVAL_TYPE_SIGNER);
		cval.setCert(cert);
		sig.addCertValue(cval);
		CertID cid = new CertID(sig, cert, CertID.CERTID_TYPE_SIGNER);
		sig.addCertID(cid);
		// create signed properties
		SignedProperties sp = new SignedProperties(sig, cert, claimedRoles, adr);
		sp.setId("ID_"+UUID.randomUUID().toString());

		Reference ref = new Reference(si, sp);
		ref.setUri("#"+sp.getId());
		si.addReference(ref);
		sig.setSignedInfo(si);
		sig.setSignedProperties(sp);
		addSignature(sig);
		return sig;
	}



	/**
	 * Helper method to create the xml header
	 * 
	 * @return xml header
	 */
	private String xmlHeader() {
		StringBuffer sb = new StringBuffer(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<document-signatures xmlns=\"");
		sb.append(getFormat());
		sb.append("\">\n");
		return sb.toString();
	}

	/**
	 * Helper method to create the xml trailer
	 * 
	 * @return xml trailer
	 */
	private String xmlTrailer() {
		return "\n</document-signatures>";
	}




	//ROB: From uji
	private static void writeXML(OutputStream outStream, Node node,
			boolean indent) throws TransformerFactoryConfigurationError,
			TransformerException {
		writeXML(new BufferedWriter(new OutputStreamWriter(outStream, Charset
				.forName("UTF-8"))), node, indent);
	}

	private static void writeXML(Writer writer, Node node, boolean indent)
			throws TransformerFactoryConfigurationError, TransformerException {
		Transformer serializer = TransformerFactory.newInstance()
				.newTransformer();
		serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

		if (indent) {
			serializer.setOutputProperty(OutputKeys.INDENT, "yes");
		}
		serializer.transform(new DOMSource(node), new StreamResult(writer));
	}
}
