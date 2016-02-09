package com.wordcount.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.springframework.stereotype.Service;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;

@Service
public class TextSearchService {

	/**
	 * Method to search count of provided json text
	 * 
	 * @param searchText
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public Map<String, Integer> searchText(String searchText)
			throws JsonParseException, JsonMappingException, IOException {

		Map<String, Integer> resultMap = new HashMap<String, Integer>();

		if (null != searchText && "" != searchText) {

			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readTree(searchText);
			JsonNode nameNode = rootNode.path("searchText");

			Iterator itr = nameNode.elements();

			System.out.println("Iterator elements found");

			while (itr.hasNext()) {

				TextNode text = (TextNode) itr.next();

				String textString = text.asText();

				System.out.println("Text Node: " + text);

				int count = getCountOfText(textString);

				System.out.println("putting values in map");

				resultMap.put(textString, count);

				System.out.println("size of map after putting values in map : " + resultMap.size());
			}

		}

		System.out.println("Map size : " + resultMap.size());

		return resultMap;
	}

	/**
	 * method performing calculations
	 * 
	 * @param text
	 * @return
	 * @throws IOException
	 */
	private int getCountOfText(String text) throws IOException {

		int count = 0;

		
		// read file
		try (BufferedReader br = new BufferedReader(new FileReader(
				System.getProperty("catalina.base") + "\\webapps\\SpringRestfulWordCounter\\resources\\testing.txt"))) {

			System.out.println("Inside try");

			String sCurrentLine;

			while ((sCurrentLine = br.readLine()) != null) {

				StringTokenizer st = new StringTokenizer(sCurrentLine, " ");

				System.out.println("Reading tokens ");
				while (st.hasMoreTokens()) {

					if (st.nextToken().equals(text)) {
						System.out.println("Inside if");
						count++;
					}

				}

			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Count for token " + text + " : " + count);

		return count;
	}

	/**
	 * Method to count the maximum occurance of tokens
	 * 
	 * @param count
	 * @return
	 */
	public File returnCount(int count) {

		File csv = new File(System.getProperty("catalina.base") + "\\webapps\\SpringRestfulWordCounter\\resources\\TopCount.csv");

		// read file
		try (BufferedReader br = new BufferedReader(new FileReader(System.getProperty("catalina.base")
				+ "\\webapps\\SpringRestfulWordCounter\\resources\\testing.txt"))) {

			String sCurrentLine;
			Set<String> tokenSet = new HashSet<String>();
			Map<String, Integer> resultMap = new HashMap<String, Integer>();

			while ((sCurrentLine = br.readLine()) != null) {

				StringTokenizer st = new StringTokenizer(sCurrentLine, " ");

				while (st.hasMoreTokens()) {

					tokenSet.add(st.nextToken());
				}

			}

			System.out.println("Total distinct Tokens in the file are: " + tokenSet.size());

			int occurrance = 0;
			// create map
			for (String token : tokenSet) {

				occurrance = getCountOfText(token);

				System.out.println("Count for token " + token + " is " + occurrance);

				resultMap.put(token, occurrance);

			}

			System.out.println("Size of map after creation " + resultMap.size());

			// Sort the map

			Map sortedResultMap = sortByValue(resultMap);

			System.out.println("size of map after sorting " + sortedResultMap.size());

			// Supercsv api code
			generateCsvFile(count, sortedResultMap);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return csv;

	}

	/**
	 * Method to generate csv file
	 * 
	 * @param count
	 * @param sortedResultMap
	 * @throws IOException
	 */
	private void generateCsvFile(int count, Map sortedResultMap) throws IOException {

		String currentRelativePath = new File("").getAbsoluteFile().toString();
		System.out.println("currentRelativePath " + currentRelativePath);

		ICsvMapWriter mapWriter = null;

		try {
			mapWriter = new CsvMapWriter(
					new FileWriter(System.getProperty("catalina.base")
							+ "\\webapps\\SpringRestfulWordCounter\\resources\\TopCount.csv"),
					CsvPreference.STANDARD_PREFERENCE);

			final CellProcessor[] processors = getProcessors();
			final String[] header = new String[] { "Search Text", "Count" };
			// write the header
			mapWriter.writeHeader(header);

			Iterator itr = sortedResultMap.entrySet().iterator();
			Map<String, Object> csvMap = new HashMap<String, Object>();

			int counter = 0;
			while (itr.hasNext() && counter < count) {
				System.out.println("counter : count " + counter + " : " + count);
				Map.Entry pair = (Map.Entry) itr.next();

				System.out.println(pair.getKey() + " = " + pair.getValue());

				csvMap.put(header[0], pair.getKey());
				csvMap.put(header[1], pair.getValue());

				mapWriter.write(csvMap, header, processors);

				counter++;

			}

		} finally {
			if (mapWriter != null) {
				mapWriter.close();
			}
		}
	}

	/**
	 * utility method of supercsv
	 * 
	 * @return
	 */
	private static CellProcessor[] getProcessors() {

		final CellProcessor[] processors = new CellProcessor[] { new Optional(), // Text
				new Optional() // count
		};

		return processors;
	}

	/**
	 * Methor to sort the map
	 * 
	 * @param unsortedMap
	 * @return
	 */
	public static Map sortByValue(Map unsortedMap) {
		Map sortedMap = new TreeMap(new ValueComparator(unsortedMap));
		sortedMap.putAll(unsortedMap);
		return sortedMap;
	}

}

/**
 * @author Vaidya class to create value comparator of map
 */
class ValueComparator implements Comparator {

	Map map;

	public ValueComparator(Map map) {
		this.map = map;
	}

	public int compare(Object keyA, Object keyB) {
		Comparable valueA = (Comparable) map.get(keyA);
		Comparable valueB = (Comparable) map.get(keyB);

		System.out.println("ValueA : ValueB : compare " + valueA + " : " + valueB + " : " + valueB.compareTo(valueA));

		if (valueB.compareTo(valueA) == 0) {
			return 1;
		} else {
			return valueB.compareTo(valueA);
		}

	}
}