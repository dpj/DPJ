DIRS= Installation Tutorial Reference/v1.0 Reference/v1.1 Spec

all :
	@(for dir in $(DIRS); do make -C $$dir; done)

clean :
	@(for dir in $(DIRS); do make -C $$dir clean; done)

publish :
	@(for dir in $(DIRS); do make -C $$dir publish; done)
