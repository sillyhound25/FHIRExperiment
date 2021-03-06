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
public class HeartRate extends BaseMeasurement{
    public static String EVENT_TYPE_NAME = "HeartRate";

    private Date time;
	private int beatsPerMinute;
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



    public HeartRate() {
	}


	public HeartRate(final Date time, final int beatsPerMinute, final String id,final String deviceName) {
		super();
		setId(id);
		this.time = time;
		this.beatsPerMinute = beatsPerMinute;
        this.deviceName = deviceName;
	}

	public Date getTime() {
		return this.time;
	}

	public void setTime(final Date time) {
		this.time = time;
	}

	public int getBeatsPerMinute() {
		return this.beatsPerMinute;
	}

	public void setBeatsPerMinute(final int beatsPerMinute) {
		this.beatsPerMinute = beatsPerMinute;
	}

}
