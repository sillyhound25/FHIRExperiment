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
    public static String EVENT_TYPE_NAME = "StepCount";
	private Date start;
	private Date end;
	private int numberOfSteps;
    private String deviceName;
    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    @Override
    public String getEventTypeName() {
        return EVENT_TYPE_NAME;
    }



    public StepCount() {
	}

	public StepCount(final Date start, final Date end, final int numberOfSteps,
			final String id, final String deviceName) {
		super();
		this.start = start;
		this.end = end;
		this.numberOfSteps = numberOfSteps;
        this.deviceName = deviceName;
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
