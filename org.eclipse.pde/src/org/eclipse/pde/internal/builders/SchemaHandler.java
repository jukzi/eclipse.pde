/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.builders;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;

import org.eclipse.core.resources.*;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.Document;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * @author melhem
 *
 */
public class SchemaHandler extends XMLErrorReporter {
	
	private org.w3c.dom.Document fDocument;
	private IDocument fTextDocument;
	private FindReplaceDocumentAdapter fFindReplaceAdapter;
	private Locator fLocator;
	private Hashtable fLineTable;
	private Element fRootElement;
	
	private Stack fElementStack = new Stack();
	
	public SchemaHandler(IFile file) {
		super(file);
		createTextDocument(file);
		fLineTable = new Hashtable();
	}
	
	public void startElement(String uri, String localName, String qName, Attributes attributes)
		throws SAXException {
		Element element = fDocument.createElement(qName);
		for (int i = 0; i < attributes.getLength(); i++) {
			element.setAttribute(attributes.getQName(i), attributes.getValue(i));
		}
		
		Integer lineNumber = new Integer(fLocator.getLineNumber());
		try {
			lineNumber = getCorrectStartLine(qName);
		} catch (BadLocationException e) {
		}
		Integer[] range = new Integer[] {lineNumber, new Integer(-1)};
		fLineTable.put(element, range);
		if (fRootElement == null)
			fRootElement = element;
		else 
			((Element)fElementStack.peek()).appendChild(element);
		fElementStack.push(element);
	}
	
	public void endElement(String arg0, String arg1, String arg2) throws SAXException {
		Integer[] range = (Integer[])fLineTable.get(fElementStack.pop());
		range[1] = new Integer(fLocator.getLineNumber());
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#setDocumentLocator(org.xml.sax.Locator)
	 */
	public void setDocumentLocator(Locator locator) {
		fLocator = locator;
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startDocument()
	 */
	public void startDocument() throws SAXException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			fDocument = factory.newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
		}
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endDocument()
	 */
	public void endDocument() throws SAXException {
		fDocument.appendChild(fRootElement);
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	public void characters(char[] characters, int offset, int length) throws SAXException {
		StringBuffer buff = new StringBuffer();
		for (int i = 0; i < length; i++) {
			buff.append(characters[offset + i]);
		}
		Text text = fDocument.createTextNode(buff.toString());
		if (fRootElement == null)
			fDocument.appendChild(text);
		else 
			((Element)fElementStack.peek()).appendChild(text);
	}
	
	public Node getDocumentElement() {
		if (fDocument != null) {
			Element docElement = fDocument.getDocumentElement();
			if (docElement != null) {
				docElement.normalize();
				return docElement;
			}
		}
		return null;
	}
	
	public Hashtable getLineTable() {
		return fLineTable;
	}
	
	private void createTextDocument(IFile file) {
		try {
			BufferedReader reader =
				new BufferedReader(new FileReader(file.getLocation().toOSString()));
			StringBuffer buffer = new StringBuffer();
			while (reader.ready()) {
				String line = reader.readLine();
				if (line != null) {
					buffer.append(line);
					buffer.append(System.getProperty("line.separator")); //$NON-NLS-1$
				}
			}
			fTextDocument = new Document(buffer.toString());
			fFindReplaceAdapter = new FindReplaceDocumentAdapter(fTextDocument);
			reader.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} 		
	}
	
	private Integer getCorrectStartLine(String elementName) throws BadLocationException{
		int lineNumber = fLocator.getLineNumber();
		int colNumber = fLocator.getColumnNumber();
		if (colNumber < 0)
			colNumber = getLastCharColumn(lineNumber);
		int offset = fTextDocument.getLineOffset(lineNumber - 1) + colNumber - 1;
		IRegion region = fFindReplaceAdapter.find(offset, "<" + elementName, false, false, false, false); //$NON-NLS-1$
		return new Integer(fTextDocument.getLineOfOffset(region.getOffset()) + 1);
	}
	
	private int getLastCharColumn(int line) throws BadLocationException {
		String lineDelimiter = fTextDocument.getLineDelimiter(line - 1);
		int lineDelimiterLength = lineDelimiter != null ? lineDelimiter.length() : 0;
		return fTextDocument.getLineLength(line - 1) - lineDelimiterLength;
	}
	
}
