/*-
 * Copyright (c) 2011, 2021 Oracle and/or its affiliates. All rights reserved.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 *  https://oss.oracle.com/licenses/upl/
 */

package oracle.nosql.driver;

import java.util.function.Consumer;

import oracle.nosql.driver.values.FieldValue;

/**
 * <p>This interface allows user to setup the collection of driver statistics.</p><p>
 * 
 * The statistics data is collected for an interval of time. At the end of the
 * interval, the stats data is logged in a specified JSON format that can be
 * filtered and parsed. After the logging, the counters are cleared and
 * collection of data resumes.</p><p>
 *
 * Collection intervals are aligned to the top of the hour. This means first
 * interval logs may contain stats for a shorter interval.</p><p>
 *
 * Collection of stats are controlled by the following system properties:<li>
 *   -Dcom.oracle.nosql.sdk.nosqldriver.stats.profile=[none|regular|more|all]
 *      Specifies the stats profile: <i>none</i> - disabled,
 *      <i>regular</i> - per request: counters, errors, latencies, delays, retries
 *      <i>more</i> - stats above with 95th and 99th percentile latencies
 *      <i>all</i> - stats above with per query information.</li><li>
 *
 *   -Dcom.oracle.nosql.sdk.nosqldriver.stats.interval=600 Interval in
 *   seconds to log the stats, by default is 10 minutes.</li><li>
 *
 *   -Dcom.oracle.nosql.sdk.nosqldriver.stats.pretty-print=true Option
 *   to enable pretty printing of the JSON data, default value is false</li></p>
 *
 * Statistics can also be enabled by using the API:
 * {@link NoSQLHandleConfig#setStatsProfile(Profile)} or
 * {@link StatsControl#setProfile(Profile)}. At runtime stats
 * collection can be used selectively by using {@link StatsControl#start()} and
 * {@link StatsControl#stop()}. The following example shows how to use a stats
 * handler: <pre>
 *     NoSQLHandleConfig config = new NoSQLHandleConfig( endpoint );
 *     config.setStatsProfile(StatsControl.Profile.REGULAR);
 *     config.setStatsInterval(600);
 *     config.setStatsPrettyPrint(false);
 *     config.registerStatsHandler(
 *         new StatsControl.StatsHandler() {
 *             public void accept(FieldValue jsonStats) {
 *                 System.out.println("!!! Got a stat: " + jsonStats);
 *             }
 *         });
 *     NoSQLHandle handle = NoSQLHandleFactory.createNoSQLHandle(config);
 *
 *     StatsControl statsControl = handle.getStatsControl();
 *
 *     //... application code without stats
 *
 *     // enable observations
 *     statsControl.start();
 *
 *     //... application code with REGULAR stats
 *
 *     // For particular parts of code profile can be changed collect more stats.
 *     statsControl.setProfile(StatsControl.Profile.ALL)
 *     //... more sensitive code with ALL stats
 *     statsControl.setProfile(StatsControl.Profile.REGULAR)
 *
 *     //... application code with REGULAR stats
 *
 *     // disable observations
 *     statsControl.stop();
 *
 *     // ... application code without stats
 *     handle.close();
 *     </pre></p><p>
 *
 * For a detailed statistics log entries structure and values see
 * {@link oracle.nosql.driver}</p>
 */
public interface StatsControl {

    /**
     * The following semantics are attached to the Profile:
     *  - NONE: no stats are logged.
     *  - REGULAR: per request: counters, errors, latencies, delays, retries
     *  - MORE: stats above with 95th and 99th percentile latencies.
     *  - ALL: stats above with per query information
     */
    enum Profile {
        NONE, REGULAR, MORE, ALL;
    }

    /**
     * Handler interface that user can register to get access to stats at
     * the end of the interval.
     */
    interface StatsHandler extends Consumer<FieldValue> {
        /** Stats are encoded in JSON format using the FieldValue API. */
        void accept(FieldValue jsonStats);
    }

    /**
     * Returns the current collection interval.
     * Default interval is 600 seconds, i.e. 10 min.
     */
    int getInterval();

    /**
     * Set the collection profile.
     * Default profile is NONE.
     *
     * @return this
     */
    StatsControl setProfile(Profile profile);

    /**
     * Returns the collection profile.
     * Default profile is NONE.
     */
    Profile getProfile();

    /**
     * Enable JSON pretty print for easier human reading.
     * Default is disabled.
     *
     * @return this
     */
    StatsControl setPrettyPrint(boolean enablePrettyPrint);

    /**
     * Returns the current JSON pretty print flag.
     * Default is disabled.
     */
    boolean getPrettyPrint();

    /**
     * Registers a stats handler.
     * @param handler User defined StatsHandler.
     *
     * @return this
     */
    StatsControl registerStatsHandler(StatsHandler handler);

    /**
     * Collection of stats is enabled only between start and stop or from the
     * beginning if system property
     * -Dcom.oracle.nosql.sdk.nosqldriver.stats.profile= is not "none".
     */
    void start();

    /**
     * Stops collection of stats.
     */
    void stop();

    /**
     * Returns true if collection of stats is enabled, otherwise returns false.
     */
    boolean isStarted();
}
