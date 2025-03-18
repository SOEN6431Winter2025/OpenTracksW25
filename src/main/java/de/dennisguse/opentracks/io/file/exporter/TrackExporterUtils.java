package de.dennisguse.opentracks.io.file.exporter;

import java.text.NumberFormat;
import java.util.Locale;

public class TrackExporterUtils {

    public static final NumberFormat ALTITUDE_FORMAT = createNumberFormat(1);
    public static final NumberFormat COORDINATE_FORMAT = createNumberFormat(6, 3);
    public static final NumberFormat SPEED_FORMAT = createNumberFormat(2);
    public static final NumberFormat DISTANCE_FORMAT = createNumberFormat(0);
    public static final NumberFormat HEARTRATE_FORMAT = createNumberFormat(0);
    public static final NumberFormat CADENCE_FORMAT = createNumberFormat(0);
    public static final NumberFormat POWER_FORMAT = createNumberFormat(0);

    private static NumberFormat createNumberFormat(int maximumFractionDigits) {
        NumberFormat format = NumberFormat.getInstance(Locale.US);
        format.setMaximumFractionDigits(maximumFractionDigits);
        format.setGroupingUsed(false);
        return format;
    }

    private static NumberFormat createNumberFormat(int maximumFractionDigits, int maximumIntegerDigits) {
        NumberFormat format = createNumberFormat(maximumFractionDigits);
        format.setMaximumIntegerDigits(maximumIntegerDigits);
        return format;
    }
}