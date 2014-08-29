/**
 * Placing this file in the com.acme.domain package ensures that very
 * java.util.Date class in the com.acme.domain package should be processed by
 * com.acme.util.DateAdapter
 */
@XmlJavaTypeAdapter(value = DateAdapter.class, type = Date.class)
package com.orchestral.data.healthkit.web.data;

import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

