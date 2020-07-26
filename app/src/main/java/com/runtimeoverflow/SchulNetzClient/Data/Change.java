package com.runtimeoverflow.SchulNetzClient.Data;

import android.util.Log;

import java.util.ArrayList;

public class Change<T> {
	public static enum ChangeType{
		ADDED, MODIFIED, REMOVED
	}
	
	public T previous = null;
	public T current = null;
	
	public String varName = "";
	
	public ChangeType type = ChangeType.MODIFIED;
	
	public Change(T previous, T current, String varName, ChangeType type){
		this.previous = previous;
		this.current = current;
		
		this.varName = varName;
		
		this.type = type;
	}
	
	public static ArrayList<Change<?>> getChanges(User previous, User current){
		ArrayList<Change<?>> changes = new ArrayList<>();
		
		if(previous.balanceConfirmed != current.balanceConfirmed) changes.add(new Change<User>(previous, current, "balanceConfirmed", ChangeType.MODIFIED));
		
		int previousIndex = 0;
		int currentIndex = 0;
		while(previousIndex < previous.teachers.size() || currentIndex < current.teachers.size()){
			Teacher previousTeacher = previousIndex < previous.teachers.size() ? previous.teachers.get(previousIndex) : null;
			Teacher currentTeacher = currentIndex < current.teachers.size() ? current.teachers.get(currentIndex) : null;
			
			if(previousTeacher != null && previousTeacher.equals(currentTeacher)) {
				if(!previousTeacher.firstName.equals(currentTeacher.firstName)) changes.add(new Change<Teacher>(previousTeacher, currentTeacher, "firstName", ChangeType.MODIFIED));
				if(!previousTeacher.lastName.equals(currentTeacher.lastName)) changes.add(new Change<Teacher>(previousTeacher, currentTeacher, "lastName", ChangeType.MODIFIED));
				if((previousTeacher.mail == null ^ currentTeacher.mail == null) || (previousTeacher.mail != null && !previousTeacher.mail.equals(currentTeacher.mail))) changes.add(new Change<Teacher>(previousTeacher, currentTeacher, "mail", ChangeType.MODIFIED));
				
				previousIndex++;
				currentIndex++;
			} else if(previousTeacher != null && !current.teachers.contains(previousTeacher)) {
				changes.add(new Change<Teacher>(previousTeacher, null, "", ChangeType.REMOVED));
				
				previousIndex++;
			} else if(currentTeacher != null && !previous.teachers.contains(currentTeacher)) {
				changes.add(new Change<Teacher>(null, currentTeacher, "", ChangeType.ADDED));
				
				currentIndex++;
			}
		}
		
		previousIndex = 0;
		currentIndex = 0;
		while(previousIndex < previous.students.size() || currentIndex < current.students.size()){
			Student previousStudent = previousIndex < previous.students.size() ? previous.students.get(previousIndex) : null;
			Student currentStudent = currentIndex < current.students.size() ? current.students.get(currentIndex) : null;
			
			if(previousStudent != null && previousStudent.equals(currentStudent)) {
				if(previousStudent.gender != currentStudent.gender) changes.add(new Change<Student>(previousStudent, currentStudent, "gender", ChangeType.MODIFIED));
				if(!previousStudent.degree.equals(currentStudent.degree)) changes.add(new Change<Student>(previousStudent, currentStudent, "degree", ChangeType.MODIFIED));
				if(previousStudent.bilingual != currentStudent.bilingual) changes.add(new Change<Student>(previousStudent, currentStudent, "bilingual", ChangeType.MODIFIED));
				if(!previousStudent.className.equals(currentStudent.className)) changes.add(new Change<Student>(previousStudent, currentStudent, "className", ChangeType.MODIFIED));
				if(!previousStudent.address.equals(currentStudent.address)) changes.add(new Change<Student>(previousStudent, currentStudent, "address", ChangeType.MODIFIED));
				if(previousStudent.zipCode != currentStudent.zipCode) changes.add(new Change<Student>(previousStudent, currentStudent, "zipCode", ChangeType.MODIFIED));
				if(!previousStudent.city.equals(currentStudent.city)) changes.add(new Change<Student>(previousStudent, currentStudent, "city", ChangeType.MODIFIED));
				if(!previousStudent.phone.equals(currentStudent.phone)) changes.add(new Change<Student>(previousStudent, currentStudent, "phone", ChangeType.MODIFIED));
				if(previousStudent.dateOfBirth.getTimeInMillis() != currentStudent.dateOfBirth.getTimeInMillis()) changes.add(new Change<Student>(previousStudent, currentStudent, "dateOfBirth", ChangeType.MODIFIED));
				if(!previousStudent.additionalClasses.equals(currentStudent.additionalClasses)) changes.add(new Change<Student>(previousStudent, currentStudent, "additionalClasses", ChangeType.MODIFIED));
				if(!previousStudent.status.equals(currentStudent.status)) changes.add(new Change<Student>(previousStudent, currentStudent, "status", ChangeType.MODIFIED));
				if(!previousStudent.placeOfWork.equals(currentStudent.placeOfWork)) changes.add(new Change<Student>(previousStudent, currentStudent, "placeOfWork", ChangeType.MODIFIED));
				if(previousStudent.self != currentStudent.self) changes.add(new Change<Student>(previousStudent, currentStudent, "self", ChangeType.MODIFIED));
				
				previousIndex++;
				currentIndex++;
			} else if(previousStudent != null && !current.students.contains(previousStudent)) {
				changes.add(new Change<Student>(previousStudent, null, "", ChangeType.REMOVED));
				
				previousIndex++;
			} else if(currentStudent != null && !previous.students.contains(currentStudent)) {
				changes.add(new Change<Student>(null, currentStudent, "", ChangeType.ADDED));
				
				currentIndex++;
			}
		}
		
		previousIndex = 0;
		currentIndex = 0;
		while(previousIndex < previous.subjects.size() || currentIndex < current.subjects.size()){
			Subject previousSubject = previousIndex < previous.subjects.size() ? previous.subjects.get(previousIndex) : null;
			Subject currentSubject = currentIndex < current.subjects.size() ? current.subjects.get(currentIndex) : null;
			
			if(previousSubject != null && previousSubject.equals(currentSubject)) {
				if(!previousSubject.identifier.equals(currentSubject.identifier)) changes.add(new Change<Subject>(previousSubject, currentSubject, "identifier", ChangeType.MODIFIED));
				if((previousSubject.name == null ^ currentSubject.name == null) || (previousSubject.name != null && !previousSubject.name.equals(currentSubject.name))) changes.add(new Change<Subject>(previousSubject, currentSubject, "name", ChangeType.MODIFIED));
				if(previousSubject.confirmed != currentSubject.confirmed) changes.add(new Change<Subject>(previousSubject, currentSubject, "confirmed", ChangeType.MODIFIED));
				if(previousSubject.hiddenGrades != currentSubject.hiddenGrades) changes.add(new Change<Subject>(previousSubject, currentSubject, "hiddenGrades", ChangeType.MODIFIED));
				
				previousIndex++;
				currentIndex++;
				
				int previousSubIndex = 0;
				int currentSubIndex = 0;
				while(previousSubIndex < previousSubject.grades.size() || currentSubIndex < currentSubject.grades.size()){
					Grade previousGrade = previousSubIndex < previousSubject.grades.size() ? previousSubject.grades.get(previousSubIndex) : null;
					Grade currentGrade = currentSubIndex < currentSubject.grades.size() ? currentSubject.grades.get(currentSubIndex) : null;
					
					if(previousGrade != null && previousGrade.equals(currentGrade)) {
						if((previousGrade.date == null ^ currentGrade.date == null) || (previousGrade.date != null && previousGrade.date.getTimeInMillis() != currentGrade.date.getTimeInMillis())) changes.add(new Change<Grade>(previousGrade, currentGrade, "date", ChangeType.MODIFIED));
						if(previousGrade.grade != currentGrade.grade) changes.add(new Change<Grade>(previousGrade, currentGrade, "grade", ChangeType.MODIFIED));
						if((previousGrade.details == null ^ currentGrade.details == null) || (previousGrade.details != null && !previousGrade.details.equals(currentGrade.details))) changes.add(new Change<Grade>(previousGrade, currentGrade, "details", ChangeType.MODIFIED));
						if(previousGrade.weight != currentGrade.weight) changes.add(new Change<Grade>(previousGrade, currentGrade, "weight", ChangeType.MODIFIED));
						
						previousSubIndex++;
						currentSubIndex++;
					} else if(previousGrade != null && !currentSubject.grades.contains(previousGrade)) {
						changes.add(new Change<Grade>(previousGrade, null, "", ChangeType.REMOVED));
						
						previousSubIndex++;
					} else if(currentGrade != null && !previousSubject.grades.contains(currentGrade)) {
						changes.add(new Change<Grade>(null, currentGrade, "", ChangeType.ADDED));
						
						currentSubIndex++;
					}
				}
			} else if(previousSubject != null && !current.students.contains(previousSubject)) {
				changes.add(new Change<Subject>(previousSubject, null, "", ChangeType.REMOVED));
				
				previousIndex++;
			} else if(currentSubject != null && !previous.students.contains(currentSubject)) {
				changes.add(new Change<Subject>(null, currentSubject, "", ChangeType.ADDED));
				
				currentIndex++;
			}
		}
		
		previousIndex = 0;
		currentIndex = 0;
		while(previousIndex < previous.transactions.size() || currentIndex < current.transactions.size()){
			Transaction previousTransaction = previousIndex < previous.transactions.size() ? previous.transactions.get(previousIndex) : null;
			Transaction currentTransaction = currentIndex < current.transactions.size() ? current.transactions.get(currentIndex) : null;
			
			if(previousTransaction != null && previousTransaction.equals(currentTransaction)) {
				if(previousTransaction.date.getTimeInMillis() != currentTransaction.date.getTimeInMillis()) changes.add(new Change<Transaction>(previousTransaction, currentTransaction, "date", ChangeType.MODIFIED));
				if(previousTransaction.amount != currentTransaction.amount) changes.add(new Change<Transaction>(previousTransaction, currentTransaction, "amount", ChangeType.MODIFIED));
				
				previousIndex++;
				currentIndex++;
			} else if(previousTransaction != null && !current.transactions.contains(previousTransaction)) {
				changes.add(new Change<Transaction>(previousTransaction, null, "", ChangeType.REMOVED));
				
				previousIndex++;
			} else if(currentTransaction != null && !previous.transactions.contains(currentTransaction)) {
				changes.add(new Change<Transaction>(null, currentTransaction, "", ChangeType.ADDED));
				
				currentIndex++;
			}
		}
		
		previousIndex = 0;
		currentIndex = 0;
		while(previousIndex < previous.absences.size() || currentIndex < current.absences.size()){
			Absence previousAbsence = previousIndex < previous.absences.size() ? previous.absences.get(previousIndex) : null;
			Absence currentAbsence = currentIndex < current.absences.size() ? current.absences.get(currentIndex) : null;
			
			if(previousAbsence != null && previousAbsence.equals(currentAbsence)) {
				if(previousAbsence.startDate.getTimeInMillis() != currentAbsence.startDate.getTimeInMillis()) changes.add(new Change<Absence>(previousAbsence, currentAbsence, "startDate", ChangeType.MODIFIED));
				if(previousAbsence.endDate.getTimeInMillis() != currentAbsence.endDate.getTimeInMillis()) changes.add(new Change<Absence>(previousAbsence, currentAbsence, "endDate", ChangeType.MODIFIED));
				if((previousAbsence.additionalInformation == null ^ currentAbsence.additionalInformation == null) || (previousAbsence.additionalInformation != null && !previousAbsence.additionalInformation.equals(currentAbsence.additionalInformation))) changes.add(new Change<Absence>(previousAbsence, currentAbsence, "additionalInformation", ChangeType.MODIFIED));
				if(previousAbsence.lessonCount != currentAbsence.lessonCount) changes.add(new Change<Absence>(previousAbsence, currentAbsence, "lessonCount", ChangeType.MODIFIED));
				if(previousAbsence.excused != currentAbsence.excused) changes.add(new Change<Absence>(previousAbsence, currentAbsence, "excused", ChangeType.MODIFIED));
				
				previousIndex++;
				currentIndex++;
			} else if(previousAbsence != null && !current.absences.contains(previousAbsence)) {
				changes.add(new Change<Absence>(previousAbsence, null, "", ChangeType.REMOVED));
				
				previousIndex++;
			} else if(currentAbsence != null && !previous.absences.contains(currentAbsence)) {
				changes.add(new Change<Absence>(null, currentAbsence, "", ChangeType.ADDED));
				
				currentIndex++;
			}
		}
		
		return changes;
	}
}
