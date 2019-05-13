package com.widas.communicator.di;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;
import javax.inject.Scope;

/**
 * Created by Widas Manasa on 02-01-2018.
 */

@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface PerCommunicator {
}
