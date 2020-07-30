package com.runtimeoverflow.SchulNetzClient;

import android.graphics.Color;
import android.util.Log;

import com.runtimeoverflow.SchulNetzClient.Data.Absence;
import com.runtimeoverflow.SchulNetzClient.Data.Grade;
import com.runtimeoverflow.SchulNetzClient.Data.Lesson;
import com.runtimeoverflow.SchulNetzClient.Data.Student;
import com.runtimeoverflow.SchulNetzClient.Data.Subject;
import com.runtimeoverflow.SchulNetzClient.Data.Teacher;
import com.runtimeoverflow.SchulNetzClient.Data.Transaction;
import com.runtimeoverflow.SchulNetzClient.Data.User;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class Parser {
	public static boolean parseGrades(Document doc, User user){
		try{
			Elements dataTables = doc.getElementsByClass("mdl-data-table");
			if(dataTables.size() <= 0 || dataTables.get(0).children().size() <= 0) return false;
			Element tableBody = dataTables.get(0).child(0);

			if(tableBody.children().size() <= 0) return false;
			Elements rows = tableBody.children();
			rows.remove(0);

			if(rows.size() % 3 != 0) return false;
			for(int i = 0; i < rows.size(); i += 3){
				Element mainRow = rows.get(i);
				Element gradesRow = rows.get(i + 1);

				if(mainRow.children().size() < 5 || mainRow.children().get(0).children().size() <= 0 || !mainRow.children().get(0).hasText() || !mainRow.children().get(0).children().get(0).hasText()) continue;
				Subject s = user.subjectForIdentifier(mainRow.children().get(0).children().get(0).ownText().trim());
				if(s == null) continue;
				
				s.name = mainRow.children().get(0).ownText().trim();
				s.confirmed = mainRow.children().get(3).children().size() <= 0 || !mainRow.children().get(3).children().get(0).hasAttr("href");
				
				ArrayList<Grade> grades = new ArrayList<>();
				
				if(gradesRow.children().size() <= 0) continue;
				if(gradesRow.children().get(0).children().size() > 0 && gradesRow.children().get(0).children().get(0).children().size() > 0){
					Element gradesTable = gradesRow.children().get(0).children().get(0).children().get(0);
					if(gradesTable.children().size() <= 0) continue;
					Elements gradeRows = gradesTable.children();
					gradeRows.remove(0);

					for(Element gradeRow : gradeRows){
						if(gradeRow.children().size() < 4) continue;
						Grade g = new Grade();

						if(gradeRow.children().get(0).hasText()) {
							SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
							g.date = Calendar.getInstance();
							g.date.setTime(sdf.parse(gradeRow.children().get(0).ownText().trim()));
						}

						if(gradeRow.children().get(1).hasText()) {
							g.content = gradeRow.children().get(1).ownText().trim();
						}

						if(gradeRow.children().get(2).hasText()) {
							g.grade = Double.parseDouble(gradeRow.children().get(2).ownText().trim());

							if(gradeRow.children().get(2).children().size() >= 2 && gradeRow.children().get(2).children().get(1).hasText()) {
								g.details = gradeRow.children().get(2).children().get(1).ownText().trim();
							}
						}

						if(gradeRow.children().get(3).hasText()) {
							g.weight = Double.parseDouble(gradeRow.children().get(3).ownText().trim());
						}

						grades.add(g);
					}
				}
				
				s.grades = grades;
			}
		} catch(Exception e){
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public static boolean parseSubjects(Document doc, User user){
		ArrayList<Subject> previous = user.subjects;
		user.subjects = new ArrayList<>();
		
		try{
			Element list = doc.getElementById("clsList");
			if(list == null) {
				user.subjects = previous;
				return false;
			}

			for(Element subject : list.children()){
				if(subject.getElementsByClass("mdl-radio__label").size() <= 0) continue;
				Subject s = new Subject();
				
				s.identifier = subject.getElementsByClass("mdl-radio__label").get(0).ownText().trim();
				
				String[] parts = s.identifier.split("-");
				if(parts.length >= 3) s.shortName = parts[0];
				
				user.subjects.add(s);
			}
		} catch(Exception e){
			e.printStackTrace();
			user.subjects = previous;
			return false;
		}

		return true;
	}

	public static boolean parseStudents(Document doc, User user){
		ArrayList<Student> previous = user.students;
		user.students = new ArrayList<>();
		
		try{
			Element table = doc.getElementById("cls-table-Kursliste");
			if(table == null || table.children().size() < 2) {
				user.students = previous;
				return false;
			}

			Elements rows = table.children().get(1).children();
			if(rows.size() < 2) {
				user.students = previous;
				return false;
			} else if(rows.size() == 2) return true;
			
			rows.remove(0);
			rows.remove(0);

			for(Element row : rows){
				if(row.children().size() < 15) continue;

				Student s = new Student();
				s.lastName = row.children().get(1).ownText().trim();
				s.firstName = row.children().get(2).ownText().trim();
				s.gender = Objects.equals(row.children().get(3).ownText().trim().toLowerCase(), "w".toLowerCase());
				s.degree = row.children().get(4).ownText().trim();
				s.bilingual = Objects.equals(row.children().get(5).ownText().trim().toLowerCase(), "b".toLowerCase());
				s.className = row.children().get(6).ownText().trim();
				s.address = row.children().get(7).ownText().trim();
				s.zipCode = row.children().get(8).ownText().trim().length() > 0 ? Integer.parseInt(row.children().get(8).ownText().trim()) : -1;
				s.city = row.children().get(9).ownText().trim();
				s.phone = row.children().get(10).ownText().trim();
				if(row.children().get(11).ownText().trim().length() > 0){
					SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
					s.dateOfBirth = Calendar.getInstance();
					s.dateOfBirth.setTime(sdf.parse(row.children().get(11).ownText().trim()));
				}
				s.additionalClasses = row.children().get(12).ownText().trim();
				s.status = row.children().get(13).ownText().trim();
				s.placeOfWork = row.children().get(14).ownText().trim();

				user.students.add(s);
			}
		} catch(Exception e){
			e.printStackTrace();
			user.students = previous;
			return false;
		}

		return true;
	}

	public static boolean parseTeachers(Document doc, User user){
		ArrayList<Teacher> previous = user.teachers;
		user.teachers = new ArrayList<>();
		
		try{
			Element table = doc.getElementById("cls-table-Lehrerliste");
			if(table == null || table.children().size() < 2) {
				user.teachers = previous;
				return false;
			}

			Elements rows = table.children().get(1).children();
			if(rows.size() < 2) {
				user.teachers = previous;
				return false;
			} else if(rows.size() == 2) return true;
			
			rows.remove(0);
			rows.remove(0);

			for(Element row : rows){
				if(row.children().size() < 5) continue;

				Teacher t = new Teacher();
				t.lastName = row.children().get(1).ownText().trim();
				t.firstName = row.children().get(2).ownText().trim();
				t.shortName = row.children().get(3).ownText().trim();
				t.mail = row.children().get(4).getElementsByTag("a").size() > 0 ? row.children().get(4).getElementsByTag("a").get(0).ownText().trim() : "";

				user.teachers.add(t);
			}
		} catch(Exception e){
			e.printStackTrace();
			user.teachers = previous;
			return false;
		}

		return true;
	}

	public static boolean parseSelf(Document doc, User user){
		try{
			Element card = doc.getElementById("content-card");
			if(card == null || card.getElementsByTag("table").size() < 2 || card.getElementsByTag("table").get(0).children().size() <= 0) return false;

			Element tableBody = card.getElementsByTag("table").get(0).children().get(0);
			if(tableBody.children().size() < 2 || tableBody.children().get(0).children().size() < 2 || tableBody.children().get(1).children().size() < 2) return false;
			
			String lastName = tableBody.children().get(0).children().get(1).ownText().trim();
			String firstName = tableBody.children().get(1).children().get(1).ownText().trim();
			
			boolean found = false;
			for(Student s : user.students){
				if(Objects.equals(s.firstName.toLowerCase(), firstName.toLowerCase()) && Objects.equals(s.lastName.toLowerCase(), lastName.toLowerCase())){
					s.self = true;
					found = true;
					break;
				}
			}

			if(!found) return false;
		} catch(Exception e){
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public static boolean parseTransactions(Document doc, User user){
		ArrayList<Transaction> previous = user.transactions;
		user.transactions = new ArrayList<>();
		
		try{
			Element card = doc.getElementById("content-card");
			if(card == null || card.getElementsByTag("table").size() < 2 || card.getElementsByTag("table").get(1).children().size() <= 0) {
				user.transactions = previous;
				return false;
			}

			Element tableBody = card.getElementsByTag("table").get(1).children().get(0);
			if(tableBody.children().size() < 2) {
				user.transactions = previous;
				return false;
			} else if(tableBody.children().size() == 2) return true;

			Elements rows = tableBody.children();
			rows.remove(0);
			rows.remove(rows.size() - 1);

			for(Element row : rows){
				if(row.children().size() < 4) continue;

				Transaction t = new Transaction();

				if(row.children().get(0).ownText().trim().length() > 0){
					SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
					t.date = Calendar.getInstance();
					t.date.setTime(sdf.parse(row.children().get(0).ownText().trim()));
				}

				t.reason = row.children().get(1).ownText().trim();
				if(row.children().get(2).children().size() > 0){
					t.amount = Double.parseDouble(row.children().get(2).children().get(0).ownText().trim().replace("sFr", ""));
				}

				user.transactions.add(t);
			}

			if(card.getElementsByTag("p").size() <= 0) return false;
			user.balanceConfirmed = card.getElementsByTag("p").get(0).getElementsByTag("a").size() <= 0;
		} catch(Exception e){
			e.printStackTrace();
			user.transactions = previous;
			return false;
		}

		return true;
	}
	
	public static boolean parseAbsences(Document doc, User user){
		ArrayList<Absence> previous = user.absences;
		user.absences = new ArrayList<>();
		
		try{
			if(doc.getElementsByTag("table").size() < 2 || doc.getElementsByTag("table").get(0).children().size() < 2) {
				user.absences = previous;
				return false;
			}
			
			Element tableBody = doc.getElementsByTag("table").get(0).children().get(1);
			if(tableBody.children().size() < 4) {
				user.absences = previous;
				return false;
			} else if(tableBody.children().size() == 4) return true;
			
			Elements rows = tableBody.children();
			rows.remove(0);
			rows.remove(rows.size() - 1);
			rows.remove(rows.size() - 1);
			rows.remove(rows.size() - 1);
			
			for(Element row : rows){
				if(row.children().size() < 7) continue;
				
				Absence a = new Absence();
				
				SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
				
				a.startDate = Calendar.getInstance();
				a.startDate.setTime(sdf.parse(row.children().get(0).ownText().trim()));
				
				a.endDate = Calendar.getInstance();
				a.endDate.setTime(sdf.parse(row.children().get(1).ownText().trim()));
				
				a.reason = row.children().get(2).ownText().trim();
				a.additionalInformation = row.children().get(3).ownText().trim();
				
				if(row.children().get(4).children().size() > 0){
					a.lessonCount = Integer.parseInt(row.children().get(4).children().get(0).ownText().trim());
				}
				
				if(row.children().get(4).children().size() > 1){
					String[] reports = row.children().get(4).children().get(1).html().replaceAll("<br>", "").split("\n");
					if(reports.length > 1){
						for(String report : reports) {
							if(report.split(",").length > 2){
								a.subjectIdentifiers.add(report.split(",")[2].trim());
							}
						}
					}
				}
				
				a.excused = Objects.equals(row.children().get(6).ownText().trim().toLowerCase(), "Ja".toLowerCase());
				
				user.absences.add(a);
			}
		} catch(Exception e){
			e.printStackTrace();
			user.absences = previous;
			return false;
		}
		
		return true;
	}
	
	public static boolean parseSchedulePage(Document doc, User user){
		try{
			if(doc.getElementById("extinst") == null) return false;
			
			Elements types = doc.getElementById("extinst").siblingElements().tagName("a");
			for(Element type : types){
				if(!type.hasAttr("href")) continue;
				else if(!type.attr("href").contains("=")) continue;
				else if(type.attr("href").lastIndexOf("=") + 1 >= type.attr("href").length()) continue;
				
				String shortName = type.attr("href").substring(type.attr("href").lastIndexOf("=") + 1);
				user.lessonTypeMap.put(shortName, type.ownText());
			}
			
			if(!doc.html().contains("var zimmerliste = [{")) return false;
			String jsDict = doc.html().substring(doc.html().indexOf("var zimmerliste = [{") + "var zimmerliste = [{".length());
			jsDict = jsDict.substring(0, jsDict.indexOf("}];"));
			
			String[] entries = jsDict.split("},\\{");
			for(String entry : entries){
				if(!entry.matches("\".*\":[0-9]*,\".*\":\".*\"")) continue;
				
				Integer key = Integer.parseInt(entry.substring(entry.indexOf(":") + 1, entry.indexOf(",")));
				String value = entry.substring(entry.indexOf("\":\"") + "\":\"".length(), entry.length() - 1);
				user.roomMap.put(key, value);
			}
		} catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public static ArrayList<Lesson> parseSchedule(Document doc){
		ArrayList<Lesson> list = new ArrayList<>();
		
		try{
			for(Element event : doc.getElementsByTag("event")){
				Lesson lesson = new Lesson();
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				
				lesson.startDate = Calendar.getInstance();
				if(event.getElementsByTag("start_date").size() > 0) lesson.startDate.setTime(sdf.parse(event.getElementsByTag("start_date").get(0).ownText()));
				
				lesson.endDate = Calendar.getInstance();
				if(event.getElementsByTag("end_date").size() > 0) lesson.endDate.setTime(sdf.parse(event.getElementsByTag("end_date").get(0).ownText()));
				
				if(event.getElementsByTag("text").size() > 0) lesson.lessonIdentifier = event.getElementsByTag("text").get(0).ownText();
				if(event.getElementsByTag("zimmer").size() > 0 && !event.getElementsByTag("zimmer").get(0).ownText().isEmpty()) lesson.roomNumber = Integer.parseInt(event.getElementsByTag("zimmer").get(0).ownText());
				if(event.getElementsByTag("color").size() > 0) lesson.color = Color.parseColor(event.getElementsByTag("color").get(0).ownText());
				if(event.getElementsByTag("event_type").size() > 0) lesson.type = event.getElementsByTag("event_type").get(0).ownText();
				
				if(event.getElementsByTag("markierung").size() > 0) lesson.marking = event.getElementsByTag("markierung").get(0).ownText();
				if(lesson.marking.equals("none")) lesson.marking = "";
				
				if(event.getElementsByTag("neuerlehrer").size() > 0) lesson.replacementTeacher = event.getElementsByTag("neuerlehrer").get(0).ownText();
				
				list.add(lesson);
			}
		} catch(Exception e){
			e.printStackTrace();
			return new ArrayList<>();
		}
		
		return list;
	}
}