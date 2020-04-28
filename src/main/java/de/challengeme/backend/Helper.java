package de.challengeme.backend;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Helper {
	public static long getAsLong(Object object, long defaultValue) {
		if (object instanceof Number) {
			return ((Number) object).longValue();
		}
		if (object instanceof Boolean) {
			return ((Boolean) object) ? 1l : 0l;
		}
		if (object instanceof String && testForNumber(((String) object)) == 1) {
			return Long.parseLong((String) object);
		}
		return defaultValue;
	}

	public static int getAsInteger(Object object, int defaultValue) {
		if (object instanceof Number) {
			return ((Number) object).intValue();
		}
		if (object instanceof Boolean) {
			return ((Boolean) object) ? 1 : 0;
		}
		if (object instanceof String && testForNumber(((String) object)) == 1) {
			return Integer.parseInt((String) object);
		}
		return defaultValue;
	}

	public static int getAsInteger(String string, int defaultValue) {
		if (string != null && testForNumber((string)) == 1) {
			return Integer.parseInt(string);
		}
		return defaultValue;
	}

	public static Integer getAsIntegerOrNull(Object object) {
		if (object instanceof Number) {
			if (object instanceof Integer) {
				return (Integer) object;
			}
			return ((Number) object).intValue();
		}
		if (object instanceof Boolean) {
			return ((Boolean) object) ? 1 : 0;
		}
		if (object instanceof String && testForNumber(((String) object)) == 1) {
			return Integer.valueOf((String) object);
		}
		return null;
	}

	/**
	 * Detects if a string contains a number.
	 * 
	 * @param string
	 * @return 2 = double, 1 = int, 0 = no number
	 */
	public static int testForNumber(String string) {
		int length = string.length();
		if (length == 0) {
			return 0;
		}
		boolean foundPoint = false;
		for (int index = 0; index < length; index++) {
			char c = string.charAt(index);
			if (c < '0' || c > '9') {
				if (c != '-' || index != 0 || length == 1) {
					if (c == '.' && !foundPoint) {
						foundPoint = true;
					} else {
						if (c == 'e' && ++index < length) {
							c = string.charAt(index);
							if (c == '+' || c == '-') { // double exponent type, check if numbers till end -> double otherwise no number
								while (++index < length) {
									c = string.charAt(index);
									if (c < '0' || c > '9') {
										return 0;
									}
								}
								return 2;
							}
						}
						return 0;
					}
				}
			}
		}
		return foundPoint ? 2 : 1;
	}

	public static double getAsDouble(Object object, double defaultValue) {
		if (object instanceof Number) {
			return ((Number) object).doubleValue();
		}
		if (object instanceof Boolean) {
			return ((Boolean) object) ? 1d : 0d;
		}
		if (object instanceof String && testForNumber((String) object) > 0) {
			return Double.parseDouble((String) object);
		}
		return defaultValue;
	}

	public static Date getAsDateOrNull(Object object) {
		if (object instanceof Number) {
			return new Date(((Number) object).longValue());
		}
		if (object instanceof String) {
			try {
				String string = (String) object;
				if ("(null)".equals(string)) {
					return null;
				}
				return new Date(Long.parseLong(string));
			} catch (NumberFormatException e) {
			}
		}
		return null;
	}

	public static String dateToString(Date date) {
		return date == null ? "(null)" : String.valueOf(date.getTime());
	}

	@SuppressWarnings("unchecked")
	public static <E> List<E> getAsList(Object object, List<?> defaultValue) {
		if (object instanceof List) {
			return (List<E>) object;
		}
		return (List<E>) defaultValue;
	}

	@SuppressWarnings({"rawtypes"})
	public static List getAsList(Object object) {
		if (object instanceof List) {
			return (List) object;
		}
		return new ArrayList<>();
	}

	public static boolean getAsBoolean(Object object, boolean defaultValue) {
		if (object instanceof Boolean) {
			return (Boolean) object;
		}

		if (object instanceof Number) {
			object = object.toString();
		}

		if (object instanceof String) {
			return getAsBoolean((String) object, defaultValue);
		}

		return defaultValue;
	}

	public static boolean getAsBoolean(String s1, boolean defaultValue) {
		if ("true".equalsIgnoreCase(s1)) {
			return true;
		}
		if ("false".equalsIgnoreCase(s1)) {
			return false;
		}
		return getAsInteger(s1, defaultValue ? 1 : 0) > 0;
	}

	public static String getAsString(Object object, String defaultValue) {
		if (object != null) {
			return object.toString();
		}
		return defaultValue;
	}

	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> getAsMap(Object object, Map<?, ?> defaultValue) {
		if (object instanceof Map) {
			return (Map<K, V>) object;
		}
		return (Map<K, V>) defaultValue;
	}

	@SuppressWarnings({"rawtypes"})
	public static Map getAsMap(Object object) {
		if (object instanceof Map) {
			return (Map) object;
		}
		return new HashMap();
	}
}
