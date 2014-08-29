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

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class StepCount extends BaseMeasurement {
	private Date start;
	private Date end;
	private int numberOfSteps;

	public StepCount() {
	}

	public StepCount(final Date start, final Date end, final int numberOfSteps,
			final String id) {
		super();
		this.start = start;
		this.end = end;
		this.numberOfSteps = numberOfSteps;
		setId(id);
	}

	public Date getStart() {
		return this.start;
	}

	public void setStart(final Date start) {
		this.start = start;
	}

	public Date getEnd() {
		return this.end;
	}

	public void setEnd(final Date end) {
		this.end = end;
	}

	public int getNumberOfSteps() {
		return this.numberOfSteps;
	}

	public void setNumberOfSteps(final int numberOfSteps) {
		this.numberOfSteps = numberOfSteps;
	}

}
