DIRS= Installation Tutorial Reference Spec

all :
	@(for dir in $(DIRS); do make -C $$dir; done)

clean :
	@(for dir in $(DIRS); do make -C $$dir clean; done)

publish :
	@(for dir in $(DIRS); do make -C $$dir publish; done)
