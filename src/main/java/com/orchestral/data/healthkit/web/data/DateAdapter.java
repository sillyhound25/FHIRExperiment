/* 
 * Copyright (c) Orchestral Developments Ltd and the Orion Health group of companies (2001 - 2014).
 * 
 * This document is copyright. Except for the purpose of fair reviewing, no part
 * of this publication may be reproduced or transmitted in any form or by any
 * means, electronic or mechanical, including photocopying, recording, or any
 * information storage and retrieval system, without permission in writing from
 * the publisher. Infringers of copyright render themselves liable for
 * prosecution.
 */
package com.orchestral.data.healthkit.web.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * <code>DateAdapter</code> is an {@link XmlAdapter} implementation that (un)marshals dates between <code>String</code> and
 * <code>Date</code> representations. All date strings meet <a href="http://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a> basic format. For
 * example, June 16, 2011 16:46:01 GMT is "20110616164601Z". Adapted from
 * http://blogs.oracle.com/CoreJavaTechTips/entry/exchanging_data_with_xml_and
 */

public class DateAdapter extends XmlAdapter<String, Date> {

	private static Logger logger = Logger
			.getLogger(DateAdapter.class.getName());
	private final SimpleDateFormat format;

	public DateAdapter() {
		this.format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		this.format.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	@Override
	public String marshal(final Date d) throws Exception {
		logger.info(d.toString());

		try {
			return this.format.format(d);
		} catch (final Exception e) {
			logger.log(Level.WARNING,
					String.format("Failed to format date %s", d.toString()), e);
			return null;
		}
	}

	@Override
	public Date unmarshal(final String d) throws Exception {
		logger.info(d);

		if (d == null) {
			return null;
		}

		try {
			return this.format.parse(d);
		} catch (final ParseException e) {
			logger.log(Level.WARNING,
					String.format("Failed to parse string %s", d), e);
			return null;
		}
	}

}
