##################################################
# epoc contact #
##################################################

include_directories(
	/System/Library/Frameworks/JavaVM.framework/Headers
)
		

#add_executable (contact.so contact.c)
add_library (contact.so contact.c)

ADD_DEPENDENCIES(contact.so emokit)
target_link_libraries (contact.so emokit ${LIBS})

SET_TARGET_PROPERTIES(contact.so PROPERTIES
INSTALL_RPATH_USE_LINK_PATH TRUE
INSTALL_RPATH ${CMAKE_INSTALL_PREFIX}/lib
BUILD_WITH_INSTALL_RPATH TRUE )

INSTALL (TARGETS contact.so
RUNTIME DESTINATION bin
ARCHIVE DESTINATION lib
)
#../../lib/libemokit.a 
#ADD_LIBRARY(contact.so SHARED contact libemokit.a)
