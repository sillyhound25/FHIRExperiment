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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Identifier {

	private String id;
	private String identifierNamespace;

	public Identifier() {
	}

	public Identifier(final String identifierNamespace, final String id) {
		super();
		this.id = id;
		this.identifierNamespace = identifierNamespace;
	}

	@XmlAttribute
	public String getIdentifierNamespace() {
		return this.identifierNamespace;
	}

	public void setIdentifierNamespace(final String namespace) {
		this.identifierNamespace = namespace;
	}

	@XmlAttribute
	public String getId() {
		return this.id;
	}

	public void setId(final String id) {
		this.id = id;
	}

}
