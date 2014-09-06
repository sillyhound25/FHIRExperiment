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
//package com.orchestral.data.healthkit.web.data;
package com.orchestral.data.healthkit.web.data;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class BloodGlucose extends BaseMeasurement {
	private Date time;
	private float value;
	private String unit;

    private String deviceName;
    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }



    public static String EVENT_TYPE_NAME = "BloodGlucose";

    @Override
    public String getEventTypeName() {
        return EVENT_TYPE_NAME;
    }

	public BloodGlucose() {
	}

	public BloodGlucose(final Date time, final float value, final String unit,
			final String id,final String deviceName) {
		super();
		this.time = time;
		this.value = value;
		this.unit = unit;
        this.deviceName = deviceName;
		setId(id);
	}

	public Date getTime() {
		return this.time;
	}

	public void setTime(final Date time) {
		this.time = time;
	}

	public float getValue() {
		return this.value;
	}

	public void setValue(final float value) {
		this.value = value;
	}

	public String getUnit() {
		return this.unit;
	}

	public void setUnit(final String unit) {
		this.unit = unit;
	}

}
