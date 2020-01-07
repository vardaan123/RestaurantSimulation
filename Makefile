JAVAC = javac
JAVAFLAGS = -g 
COMPILE = $(JAVAC) $(JAVAFLAGS)
CLASS_FILES = restaurant.class cook.class Diner.class Table.class
all: $(CLASS_FILES) 
%.class : %.java
	$(COMPILE) $<