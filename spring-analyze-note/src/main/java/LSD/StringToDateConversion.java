package LSD;

import org.springframework.core.convert.converter.Converter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StringToDateConversion implements Converter<String, Date> {

	private String datePattern;

	public String getDatePattern() {
		return datePattern;
	}

	public void setDatePattern(String datePattern) {
		this.datePattern = datePattern;
	}

	@Override
	public Date convert(String source) {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(this.datePattern);
			return dateFormat.parse(source);
		}catch (Exception e){
			e.printStackTrace();
			return null;
		}

	}
}
