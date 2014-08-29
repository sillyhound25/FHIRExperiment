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
public class BloodPressure extends BaseMeasurement implements IDapPojo {
	private String id;
	private Date time;
	private int systolicValueInMmHg;
	private int diastolicValueInMmHg;

	public BloodPressure() {
	}

	public BloodPressure(final Date time, final int systolicValueInMmHg, final int diastolicValueInMmHg, final String id) {
		super();
		setId(id);
		this.time = time;
		this.systolicValueInMmHg = systolicValueInMmHg;
		this.diastolicValueInMmHg = diastolicValueInMmHg;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public void setId(final String id) {
		this.id = id;
	}

	public Date getTime() {
		return this.time;
	}

	public void setTime(final Date time) {
		this.time = time;
	}

	public int getSystolicValueInMmHg() {
		return this.systolicValueInMmHg;
	}

	public void setSystolicValueInMmHg(final int systolicValueInMmHg) {
		this.systolicValueInMmHg = systolicValueInMmHg;
	}

	public int getDiastolicValueInMmHg() {
		return this.diastolicValueInMmHg;
	}

	public void setDiastolicValueInMmHg(final int diastolicValueInMmHg) {
		this.diastolicValueInMmHg = diastolicValueInMmHg;
	}

}
