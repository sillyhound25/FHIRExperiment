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
public class BaseMeasurement {

	private String id;
	private int dummyOrder = 1;
	private String dummySequenceId;

	public String getId() {
		return this.id;
	}

	public void setId(final String id) {
		this.id = id;
		this.dummySequenceId = id;
	}

    //return the name of the model in Data Platform to save this observation in...
    public String getModelName() {
        return "";
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
