/* 
 * Copyright (c) 1998-2003 Massachusetts Institute of Technology. 
 * This code was developed as part of the Haystack research project 
 * (http://haystack.lcs.mit.edu/). Permission is hereby granted, 
 * free of charge, to any person obtaining a copy of this software 
 * and associated documentation files (the "Software"), to deal in 
 * the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit 
 * persons to whom the Software is furnished to do so, subject to 
 * the following conditions: 
 * 
 * The above copyright notice and this permission notice shall be 
 * included in all copies or substantial portions of the Software. 
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES 
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT 
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS IN THE SOFTWARE. 
 */

package edu.mit.lcs.haystack.ozone.core.utils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import edu.mit.lcs.haystack.ozone.core.Context;
import edu.mit.lcs.haystack.proxy.IServiceAccessor;
import edu.mit.lcs.haystack.rdf.FederationRDFContainer;
import edu.mit.lcs.haystack.rdf.IRDFContainer;
import edu.mit.lcs.haystack.rdf.IRDFEventSource;
import edu.mit.lcs.haystack.rdf.RDFException;
import edu.mit.lcs.haystack.rdf.Resource;
import edu.mit.lcs.haystack.rdf.Statement;
import edu.mit.lcs.haystack.rdf.Utilities;
import edu.mit.lcs.haystack.server.core.rdfstore.RDFListener;
import edu.mit.lcs.haystack.server.core.service.ServiceException;
import edu.mit.lcs.haystack.server.core.service.ServiceManager;

/**
 * Reads information from a vc:InformationSourceSpecification and 
 * builds the proper information source.
 * @author Dennis Quan
 */
abstract public class InformationSourceManager implements Serializable {
	public static final String NAMESPACE = "http://haystack.lcs.mit.edu/ui/viewContainer#";
	public static final Resource informationSource = new Resource(NAMESPACE + "informationSource");
	public static final Resource informationSourceSpecification = new Resource(NAMESPACE + "informationSourceSpecification");
	public static final Resource detectedInformationSource = new Resource(NAMESPACE + "detectedInformationSource");
	public static final Resource primaryInformationSource = new Resource(NAMESPACE + "primaryInformationSource");
	
	transient protected IRDFContainer m_source;
	protected Context m_context;
	transient protected RDFListener m_listener = null;
	protected Resource m_infoSourceSpec;
	protected Resource m_currentResource = null;
	transient protected IRDFContainer m_currentInfoSource;
	protected HashSet m_userSpecifiedInformationSources = new HashSet();
	protected HashSet m_detectedInformationSources = new HashSet();
	protected Resource m_primaryInformationSource = null;
	
	static org.apache.log4j.Logger s_logger = org.apache.log4j.Logger.getLogger(InformationSourceManager.class);
	
	public InformationSourceManager(Context context, IRDFContainer source, Resource infoSourceSpec) {
		m_context = context;
		m_source = source;
		m_infoSourceSpec = infoSourceSpec;

		setupInfoSourceSpec();
	}
	
	public void initializeFromDeserialization(IRDFContainer source) {
		m_source = source;
		setupInfoSourceSpec();
	}

	protected void setupInfoSourceSpec() {
		//System.out.println(">> listening in on " + m_infoSourceSpec);
		if (m_infoSourceSpec != null) {		
			m_listener = new RDFListener((ServiceManager) m_context.getServiceAccessor(), (IRDFEventSource) m_source) {
				/**
				 * @see edu.mit.lcs.haystack.server.core.rdfstore.RDFListener#statementAdded(edu.mit.lcs.haystack.rdf.Resource, edu.mit.lcs.haystack.rdf.Statement)
				 */
				public void statementAdded(Resource cookie, Statement s) {
					if (!s.getPredicate().equals(detectedInformationSource)) {
						refreshSpecification();
					}
					super.statementAdded(cookie, s);
				}
	
				/**
				 * @see edu.mit.lcs.haystack.server.core.rdfstore.RDFListener#statementRemoved(edu.mit.lcs.haystack.rdf.Resource, edu.mit.lcs.haystack.rdf.Statement)
				 */
				public void statementRemoved(Resource cookie, Statement s) {
					if (!s.getPredicate().equals(detectedInformationSource)) {
						refreshSpecification();
					}
					super.statementRemoved(cookie, s);
				}
				
			};
			try {
				m_listener.addPattern(m_infoSourceSpec, null, null);
			} catch (RDFException e) {
			}
			m_listener.start();
			refreshSpecification();
		}
	}
	
	public void dispose() {
		if (m_listener != null) {
			try {
				m_listener.shutdown();
			} catch (ServiceException e) {
			}
			m_listener = null;
		}
	}
	
	protected void refreshSpecification() {
		if (m_infoSourceSpec == null) {
			return;
		}
		
		m_primaryInformationSource = Utilities.getResourceProperty(m_infoSourceSpec, primaryInformationSource, m_source);
		Resource[] secondaries = Utilities.getResourceProperties(m_infoSourceSpec, informationSource, m_source);
		
		m_userSpecifiedInformationSources.clear();
		m_userSpecifiedInformationSources.addAll(Arrays.asList(secondaries));
		
		if (m_currentResource != null) {
			m_currentInfoSource = null;
			notifyRefresh();
		}
	}

	abstract protected void notifyRefresh(); 
	
	protected void removeAllDetectedInformationSources() {
		if (m_infoSourceSpec == null) {
			return;
		}
		
		Iterator i = m_detectedInformationSources.iterator();
		while (i.hasNext()) {
			Resource res = (Resource)i.next();
			try {
				m_source.remove(new Statement(m_infoSourceSpec, detectedInformationSource, res), new Resource[] {});
			} catch (RDFException e) {
			}
		}
		m_detectedInformationSources.clear(); 
	}
	
	protected IRDFContainer connect(Resource res) {
		try {
			IServiceAccessor sa = m_context.getServiceAccessor();
			return (IRDFContainer)sa.connectToService(res, m_context.getUserIdentity());
		} catch (Exception e) {
			s_logger.error("Failed to connect to service accessor", e);
			return null;
		}
	}
	
	abstract protected void detectInformationSources(Resource resource);

	public IRDFContainer constructChildInformationSource(Resource resource) {
		if (resource == null) {
			return m_context.getInformationSource();
		}

		if ((m_currentResource != null) && m_currentResource.equals(resource) && (m_currentInfoSource != null)) {
			// If there is no change, continue using the existing information source		
			return m_currentInfoSource;
		} else {
			removeAllDetectedInformationSources();
			detectInformationSources(resource);
		}
		 
		m_currentResource = resource;
		
		FederationRDFContainer infoSource = new FederationRDFContainer();
		infoSource.addSource(m_context.getInformationSource(), 1);
		if (m_primaryInformationSource != null) {
			infoSource.addSource(connect(m_primaryInformationSource), 0);
		}

		Iterator i = m_detectedInformationSources.iterator();
		while (i.hasNext()) {
			IRDFContainer rdfc2 = connect((Resource)i.next());
			if (rdfc2 != null) {
				infoSource.addSource(rdfc2, 2);
			}
		}
		
		i = m_userSpecifiedInformationSources.iterator();
		while (i.hasNext()) {
			IRDFContainer rdfc2 = connect((Resource)i.next());
			if (rdfc2 != null) {
				infoSource.addSource(rdfc2, 2);
			}
		}
		
		m_currentInfoSource = infoSource;
		return infoSource;
	}
}
