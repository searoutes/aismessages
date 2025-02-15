/*
 * AISMessages
 * - a java-based library for decoding of AIS messages from digital VHF radio traffic related
 * to maritime navigation and safety in compliance with ITU 1371.
 *
 * (C) Copyright 2011- by S-Consult ApS, VAT no. DK31327490, Denmark.
 *
 * Released under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License.
 * For details of this license see the nearby LICENCE-full file, visit http://creativecommons.org/licenses/by-nc-sa/3.0/
 * or send a letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 *
 * NOT FOR COMMERCIAL USE!
 * Contact Thomas Borg Salling <tbsalling@tbsalling.dk> to obtain a commercially licensed version of this software.
 *
 */

package dk.tbsalling.aismessages.nmea.messages;

import dk.tbsalling.aismessages.nmea.exceptions.NMEAParseException;
import dk.tbsalling.aismessages.nmea.exceptions.UnsupportedMessageType;
import dk.tbsalling.aismessages.nmea.tagblock.NMEATagBlock;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * AISMessages
 * - a java-based library for decoding of AIS messages from digital VHF radio traffic related
 * to maritime navigation and safety in compliance with ITU 1371.
 *
 * (C) Copyright 2011- by S-Consult ApS, VAT no. DK31327490, Denmark.
 *
 * Released under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License.
 * For details of this license see the nearby LICENCE-full file, visit http://creativecommons.org/licenses/by-nc-sa/3.0/
 * or send a letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 *
 * NOT FOR COMMERCIAL USE!
 * Contact Thomas Borg Salling <tbsalling@tbsalling.dk> to obtain a commercially licensed version of this software.
 *
 */

// TODO optimize getters
public class NMEAMessage implements Serializable {

	public static NMEAMessage fromString(String nmeaString) {
		return new NMEAMessage(nmeaString);
	}

    private ZonedDateTime timestamp = null;

	public final boolean isValid() {
        String messageType = getMessageType();

        if(messageType == null || messageType.length() != 5) return false;

		String type = messageType.substring(2);
		if (! ("VDM".equals(type) || "VDO".equals(type))) {
			return false;
		}

		return true;
	}

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime ts) {
        timestamp=ts;
    }

    @SuppressWarnings("unused")
	public String getMessageType() {
        String[] msg = rawMessage.split(",");
		return isBlank(msg[0]) ? null : msg[0].replace("!", "");
	}

    @SuppressWarnings("unused")
    public Integer getNumberOfFragments() {
        String[] msg = rawMessage.split(",");
        return isBlank(msg[1]) ? null : Integer.valueOf(msg[1]);
	}

    @SuppressWarnings("unused")
    public Integer getFragmentNumber() {
        String[] msg = rawMessage.split(",");
        return isBlank(msg[2]) ? null : Integer.valueOf(msg[2]);
	}

    @SuppressWarnings("unused")
    public Integer getSequenceNumber() {
        String[] msg = rawMessage.split(",");
        return isBlank(msg[3]) ? null : Integer.valueOf(msg[3]);
	}

    @SuppressWarnings("unused")
    public String getRadioChannelCode() {
        String[] msg = rawMessage.split(",");
        return isBlank(msg[4]) ? null : msg[4];
	}

    @SuppressWarnings("unused")
    public String getEncodedPayload() {
        String[] msg = rawMessage.split(",");
        return isBlank(msg[5]) ? null : msg[5];
	}

    @SuppressWarnings("unused")
    public Integer getFillBits() {
        String[] msg = rawMessage.split(",");
        String msg1[] = msg[6].split("\\*");
        return isBlank(msg1[0]) ? null : Integer.valueOf(msg1[0]);
	}

    @SuppressWarnings("unused")
    public Integer getChecksum() {
        String[] msg = rawMessage.split(",");
        String msg1[] = msg[6].split("\\*");
		return isBlank(msg1[1]) ? null : Integer.valueOf(msg1[1], 16);
	}

    @SuppressWarnings("unused")
    public String getRawMessage() {
		return rawMessage;
	}

    public NMEATagBlock getTagBlock() {
        return tagBlock;
    }

    private NMEAMessage(String rawMessage) {
        this.rawMessage = rawMessage;
        validate();
	}

	private void validate() {
        parseNMEATagBlockString();

	    // !AIVDM,1,1,,B,15MvlfPOh2G?nwbEdVDsnSTR00S?,0*41
        final String nmeaMessageRegExp = "^!.*\\*[0-9A-Fa-f]{2}$";

        if(!isValid()) {
            throw new UnsupportedMessageType(getMessageType());
        }

        if (! rawMessage.matches(nmeaMessageRegExp))
            throw new NMEAParseException(rawMessage, "Message does not comply with regexp \"" + nmeaMessageRegExp + "\"");

        String[] msg = rawMessage.split(",");
        if (msg.length != 7)
            throw new NMEAParseException(rawMessage, "Expected 7 fields separated by commas; got " + msg.length);

        String msg1[] = msg[6].split("\\*");
        if (msg1.length != 2)
            throw new NMEAParseException(rawMessage, "Expected checksum fields to start with *");
    }

    private void parseNMEATagBlockString() {
	    final String nmeaTagBlockRegEx = "^\\\\.*\\*[0-9A-Fa-f]{2}\\\\";

        Pattern pattern = Pattern.compile(nmeaTagBlockRegEx);
        Matcher matcher = pattern.matcher(rawMessage);
        if (matcher.lookingAt())  {
            String nmeaTagBlockString = rawMessage.substring(matcher.start(), matcher.end());
            this.tagBlock = NMEATagBlock.fromString(nmeaTagBlockString);
            this.rawMessage = this.rawMessage.substring(matcher.end());
        }
        else {
            this.tagBlock = null;
        }
    }

    @Override
    public String toString() {
        return "NMEAMessage{" +
                "rawMessage='" + rawMessage + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NMEAMessage that = (NMEAMessage) o;

        if (!rawMessage.equals(that.rawMessage)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return rawMessage.hashCode();
    }

    private static boolean isBlank(String s) {
		return s == null || s.trim().length() == 0;
	}

	private String rawMessage;
	private NMEATagBlock tagBlock;
}
