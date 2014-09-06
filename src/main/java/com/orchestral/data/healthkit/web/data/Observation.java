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
public class Observation extends BaseMeasurement{
    public static String EVENT_TYPE_NAME = "Observation";

    private Date _startDate;
    private Date _endDate;

    private String _valueString;
    private OdtQuantity _valueQuantity;



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



    public Observation() {
    }


    public void setValueQuantity(OdtQuantity quantity) {
        _valueQuantity = quantity;
    }


    //private

    public void setStartDate(final Date time) {
        this._startDate = time;
    }
    public Date getStartDate() {
        return this._startDate;
    }

    public void setEndDate(final Date time) {
        this._endDate = time;
    }
    public Date getEndDate() {
        return this._endDate;
    }

}
