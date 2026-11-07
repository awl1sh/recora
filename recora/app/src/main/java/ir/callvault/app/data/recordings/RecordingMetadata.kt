/*
 * CallVault: FOSS call recording, self-contained over embedded ADB
 *  Copyright (C) 2026-present kitsumed (Med)
 *  Copyright (C) 2026-present The CallVault Authors
 *  This software is licensed under the GNU General Public License v3 or later, with additional terms as permitted under Section 7.
 *  The full license text is available in the LICENSE file at the root of this project.
 *  This software is distributed WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package ir.callvault.app.data.recordings

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import ir.callvault.app.utils.AppLogger
import ir.callvault.app.utils.PhoneNumberManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Carries the metadata associated with a single call that is being (or will be) recorded.
 *
 * **NOTE**: Enrichment fields are additional metadata that may not be available (null). Call [enrichMetadata] to attempt to fill them in after construction.
 *
 * @param rawPhoneNumber The raw phone number as received from the system, which may be in various formats or null.
 * @param direction The direction of the call (incoming or outgoing).
 * @param standardizedNumber **Enrichment**: An optional standardized E.164 phone number for better display and filename generation.
 * @param isCrossCountry **Enrichment**: An optional flag indicating if the call is cross-country.
 * @param isEnriched **Enrichment**: A flag indicating whether enrichment has been attempted on this metadata instance.
 */
data class RecordingMetadata(
    val rawPhoneNumber: String?,
    val direction: RecordingDirection,
    // -- Enrichment fields --
    val standardizedNumber: String? = null,
    val isCrossCountry: Boolean = false,
    val isEnriched: Boolean = false
) : Parcelable {
    /**
     * Returns the best available phone number for display and filename purposes.
     * Try the standardized E.164 number first, then fall back to the raw phone number if necessary.
     */
    fun getBestNumber() = standardizedNumber ?: rawPhoneNumber

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(rawPhoneNumber)
        dest.writeString(direction.name)
        dest.writeString(standardizedNumber)
        dest.writeInt(if (isCrossCountry) 1 else 0)
        dest.writeInt(if (isEnriched) 1 else 0)
    }

    companion object {
        const val TAG = "CV:RecordingMetadata"

        /**
         * The key used to pass RecordingMetadata in an Intent when starting the recording service.
         */
        const val EXTRA_METADATA = "ir.callvault.app.EXTRA_RECORDING_METADATA"

        @JvmField
        val CREATOR: Parcelable.Creator<RecordingMetadata> = object : Parcelable.Creator<RecordingMetadata> {
            override fun createFromParcel(source: Parcel): RecordingMetadata = RecordingMetadata(
                rawPhoneNumber = source.readString(),
                direction = RecordingDirection.valueOf(source.readString() ?: RecordingDirection.INCOMING.name),
                standardizedNumber = source.readString(),
                isCrossCountry = source.readInt() != 0,
                isEnriched = source.readInt() != 0
            )

            override fun newArray(size: Int): Array<RecordingMetadata?> = arrayOfNulls(size)
        }

        /**
         * Attempts to enrich the provided RecordingMetadata with additional information such as standardized phone number and cross-country status.
         * @param context The context used to access phone number utilities.
         * @param base The base RecordingMetadata to enrich.
         * @return A new RecordingMetadata instance with enrichment fields filled in where possible.
         */
        suspend fun enrichMetadata(context: Context ,base: RecordingMetadata): RecordingMetadata = withContext(Dispatchers.Default) {
            if (base.isEnriched) {
                AppLogger.w(TAG, "Attempted to enrich metadata that is already marked as enriched. Returning original.")
                return@withContext base
            }

            // Null phone number and/or anonymous call
            val raw = base.rawPhoneNumber ?: return@withContext base.copy(
                isEnriched = true,
                isCrossCountry = true // We should assume it's cross-country to be safe, we cannot know from where it is coming from.
            )

            val phoneNumberManager = PhoneNumberManager.getInstance(context)
            val parsedNumber = phoneNumberManager.parsePhoneNumber(raw)

            if (parsedNumber == null) {
                return@withContext base.copy(
                    isEnriched = true,
                    isCrossCountry = true // Could not parse, we should assume it's cross-country to be safe.
                )
            }

            if (!phoneNumberManager.isValidNumber(parsedNumber)) {
                // Short codes (e.g. the FR "123" voicemail number) parse but are not valid subscriber
                // numbers; E.164-ifying them ("+33123") breaks contact lookup and display, so keep raw.
                // Cross-country is still computed: an invalid number can carry a foreign country code,
                // and hardcoding false here would bypass the ignore-cross-country recording rules.
                AppLogger.i(TAG, "Number '$raw' is not a valid subscriber number (likely a short code); keeping raw form.")
                return@withContext base.copy(
                    isEnriched = true,
                    isCrossCountry = phoneNumberManager.isNumberFromDifferentCountry(parsedNumber)
                )
            }

            val standardized = phoneNumberManager.formatToE164(parsedNumber)
            val crossCountry = phoneNumberManager.isNumberFromDifferentCountry(parsedNumber)
            AppLogger.i(TAG, "Enriched metadata for number: raw='$raw', standardized='$standardized', crossCountry=$crossCountry")
            return@withContext base.copy(
                standardizedNumber = standardized,
                isCrossCountry = crossCountry,
                isEnriched = true
            )
        }
    }
}
