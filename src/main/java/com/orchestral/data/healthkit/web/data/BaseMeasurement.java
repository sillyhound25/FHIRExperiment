package com.orchestral.data.healthkit.web.data;

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

/**
 * All measurements should have an identifier.
 */
public abstract class BaseMeasurement {

	private String id;
	private int dummyOrder = 1;
	private String dummySequenceId;

    /*
    * return the name that this event will be published in Data Platform as
     */
    public abstract String getEventTypeName();

	public String getId() {
		return this.id;
	}

	public void setId(final String id) {
		this.id = id;
		this.dummySequenceId = id;
	}


	public String getDummySequenceId() {
		return this.dummySequenceId;
	}

	public void setDummySequenceId(final String dummySequenceId) {
		this.dummySequenceId = dummySequenceId;
	}

	public int getDummyOrder() {
		return this.dummyOrder;
	}

	public void setDummyOrder(final int dummyOrder) {
		this.dummyOrder = dummyOrder;
	}

}
