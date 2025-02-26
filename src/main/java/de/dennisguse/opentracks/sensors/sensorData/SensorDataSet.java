package de.dennisguse.opentracks.sensors.sensorData;

import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import de.dennisguse.opentracks.data.models.AtmosphericPressure;
import de.dennisguse.opentracks.data.models.Cadence;
import de.dennisguse.opentracks.data.models.HeartRate;
import de.dennisguse.opentracks.data.models.Position;
import de.dennisguse.opentracks.data.models.Speed;
import de.dennisguse.opentracks.data.models.TrackPoint;
import de.dennisguse.opentracks.sensors.BluetoothHandlerCyclingCadence;
import de.dennisguse.opentracks.sensors.BluetoothHandlerCyclingDistanceSpeed;
import de.dennisguse.opentracks.sensors.BluetoothHandlerManagerCyclingPower;
import de.dennisguse.opentracks.sensors.BluetoothHandlerRunningSpeedAndCadence;
import de.dennisguse.opentracks.services.handlers.TrackPointCreator;
import de.dennisguse.opentracks.settings.PreferencesUtils;

public class SensorDataSet {

    private static final String TAG = SensorDataSet.class.getSimpleName();

    @VisibleForTesting
    public AggregatorHeartRate heartRate;

    @VisibleForTesting
    public AggregatorCyclingCadence cyclingCadence;

    @VisibleForTesting
    public AggregatorCyclingDistanceSpeed cyclingDistanceSpeed;

    @VisibleForTesting
    public AggregatorCyclingPower cyclingPower;

    @VisibleForTesting
    public AggregatorRunning runningDistanceSpeedCadence;

    @VisibleForTesting
    public AggregatorBarometer barometer;

    public AggregatorGPS gps;

    private TrackPointCreator trackPointCreator;

    public SensorDataSet(TrackPointCreator trackPointCreator) {
        this.trackPointCreator = trackPointCreator;
    }

    @Deprecated //TODO This is not a copy constructor anymore, but it should be - aggregators are no value objects; best guess: can be removed
    public SensorDataSet(SensorDataSet toCopy) {
        this.trackPointCreator = toCopy.trackPointCreator;
        this.heartRate = toCopy.heartRate;
        this.cyclingCadence = toCopy.cyclingCadence;
        this.cyclingDistanceSpeed = toCopy.cyclingDistanceSpeed;
        this.cyclingPower = toCopy.cyclingPower;
        this.runningDistanceSpeedCadence = toCopy.runningDistanceSpeedCadence;
        this.barometer = toCopy.barometer;
        this.gps = toCopy.gps;
    }

    public Pair<HeartRate, String> getHeartRate() {
        if (heartRate != null) {
            return new Pair<>(heartRate.getValue(trackPointCreator.createNow()), heartRate.getSensorNameOrAddress());
        }

        return null;
    }

    public Pair<Cadence, String> getCadence() {
        if (cyclingCadence != null) {
            return new Pair<>(cyclingCadence.getValue(trackPointCreator.createNow()), cyclingCadence.getSensorNameOrAddress());
        }

        if (runningDistanceSpeedCadence != null && runningDistanceSpeedCadence.hasValue() && runningDistanceSpeedCadence.value.cadence() != null) {
            return new Pair<>(runningDistanceSpeedCadence.value.cadence(), runningDistanceSpeedCadence.getSensorNameOrAddress());
        }

        return null;
    }

    public Pair<Speed, String> getSpeed() {
        if (cyclingDistanceSpeed != null && cyclingDistanceSpeed.hasValue() && cyclingDistanceSpeed.getValue(trackPointCreator.createNow()).speed() != null) {
            return new Pair<>(cyclingDistanceSpeed.getValue(trackPointCreator.createNow()).speed(), cyclingDistanceSpeed.getSensorNameOrAddress());
        }

        if (runningDistanceSpeedCadence != null && runningDistanceSpeedCadence.hasValue() && runningDistanceSpeedCadence.getValue(trackPointCreator.createNow()).speed() != null) {
            return new Pair<>(runningDistanceSpeedCadence.value.speed(), runningDistanceSpeedCadence.getSensorNameOrAddress());
        }

        return null;
    }

    public AggregatorCyclingPower getCyclingPower() {
        return cyclingPower;
    }

    public void add(@NonNull Aggregator<?, ?> data) {
        set(data, data);
    }

    public void update(@NonNull Raw<?> data) {
        Object value = data.value();

        if (value instanceof HeartRate) {
            this.heartRate.add((Raw<HeartRate>) data);
            return;
        }

        if (value instanceof BluetoothHandlerCyclingCadence.CrankData) {
            this.cyclingCadence.add((Raw<BluetoothHandlerCyclingCadence.CrankData>) data);
            return;
        }
        if (value instanceof BluetoothHandlerCyclingDistanceSpeed.WheelData ) {
            this.cyclingDistanceSpeed.setWheelCircumference(PreferencesUtils.getWheelCircumference()); //TODO Fetch once and then listen for changes.
            this.cyclingDistanceSpeed.add((Raw<BluetoothHandlerCyclingDistanceSpeed.WheelData>) data);
            return;
        }
        if (value instanceof BluetoothHandlerRunningSpeedAndCadence.Data) {
            this.runningDistanceSpeedCadence.add((Raw<BluetoothHandlerRunningSpeedAndCadence.Data>) data);

            return;
        }
        if (value instanceof BluetoothHandlerManagerCyclingPower.Data) {
            this.cyclingPower.add((Raw<BluetoothHandlerManagerCyclingPower.Data>) data);
            return;
        }
        if (value instanceof AtmosphericPressure) {
            this.barometer.add((Raw<AtmosphericPressure>) data);
            return;
        }
        if (value instanceof Position) {
            if (this.gps == null) {
                return;
            }
            this.gps.add((Raw<Position>) data);
            return;
        }

        throw new UnsupportedOperationException(data.getClass().getCanonicalName() + " " + data.value().getClass().getCanonicalName());
    }

    public void remove(@NonNull Aggregator<?, ?> type) {
        set(type, null);
    }

    public void clear() {
        Log.i(TAG, "Removing all aggregators");
        this.heartRate = null;
        this.cyclingCadence = null;
        this.cyclingDistanceSpeed = null;
        this.cyclingPower = null;
        this.runningDistanceSpeedCadence = null;
        this.barometer = null;
        this.gps = null;
    }

    public void fillTrackPoint(TrackPoint trackPoint) {
        if (gps != null && gps.hasValue()) {
            trackPoint.setPosition(gps.getValue(trackPointCreator.createNow()));
        }

        if (getHeartRate() != null) {
            trackPoint.setHeartRate(getHeartRate().first);
        }

        if (getCadence() != null) {
            trackPoint.setCadence(getCadence().first);
        }

        if (getSpeed() != null) {
            trackPoint.setSpeed(getSpeed().first);
        }

        if (cyclingDistanceSpeed != null && cyclingDistanceSpeed.hasValue()) {
            trackPoint.setSensorDistance(cyclingDistanceSpeed.getValue(trackPointCreator.createNow()).distanceOverall());
        }

        if (cyclingPower != null && cyclingPower.hasValue()) {
            trackPoint.setPower(cyclingPower.getValue(trackPointCreator.createNow()));
        }

        if (runningDistanceSpeedCadence != null && runningDistanceSpeedCadence.hasValue()) {
            trackPoint.setSensorDistance(runningDistanceSpeedCadence.getValue(trackPointCreator.createNow()).distance());
        }

        if (barometer != null && barometer.hasValue()) {
            trackPoint.setAltitudeGain(barometer.getValue(trackPointCreator.createNow()).gain_m());
            trackPoint.setAltitudeLoss(barometer.getValue(trackPointCreator.createNow()).loss_m());
        }
    }

    public void reset() {
        Log.i(TAG, "Resetting data");

        if (heartRate != null) heartRate.reset();
        if (cyclingCadence != null) cyclingCadence.reset();
        if (cyclingDistanceSpeed != null) cyclingDistanceSpeed.reset();
        if (cyclingPower != null) cyclingPower.reset();
        if (runningDistanceSpeedCadence != null) runningDistanceSpeedCadence.reset();
        if (barometer != null) barometer.reset();
        if (gps != null) gps.reset();
    }

    private void set(@NonNull Aggregator<?, ?> type, @Nullable Aggregator<?, ?> sensorData) {
        Log.i(TAG, "Setting aggregator " + type.getClass().getCanonicalName() + " to " + sensorData);

        if (type instanceof AggregatorHeartRate) {
            heartRate = (AggregatorHeartRate) sensorData;
            return;
        }
        if (type instanceof AggregatorCyclingCadence) {
            cyclingCadence = (AggregatorCyclingCadence) sensorData;
            return;
        }
        if (type instanceof AggregatorCyclingDistanceSpeed) {
            cyclingDistanceSpeed = (AggregatorCyclingDistanceSpeed) sensorData;
            return;
        }
        if (type instanceof AggregatorCyclingPower) {
            cyclingPower = (AggregatorCyclingPower) sensorData;
            return;
        }
        if (type instanceof AggregatorRunning) {
            runningDistanceSpeedCadence = (AggregatorRunning) sensorData;
            return;
        }
        if (type instanceof AggregatorBarometer) {
            barometer = (AggregatorBarometer) sensorData;
            return;
        }
        if (type instanceof AggregatorGPS) {
            gps = (AggregatorGPS) sensorData;
            return;
        }

        throw new UnsupportedOperationException(type.getClass().getCanonicalName());
    }
}
