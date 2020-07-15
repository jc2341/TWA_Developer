from distutils.core import setup

setup(
    # This is the name of your project. The first time you publish this
    # package, this name will be registered for you. It will determine how
    # users can install this project, e.g.:
    #
    # $ pip install sampleproject
    #
    name='CompChemParser', # Required
    version='1.0.0', # Required    
    # This should be your name or the name of the organization which owns the
    # project.
    author='Angiras Menon', # Optional
    # This should be a valid email address corresponding to the author listed
    # above.
    author_email='',  # Optional
    license='',
    long_description=open('README.txt').read(), # Optional
    # When your source code is in a subdirectory under the project root, e.g.
    # `src/`, it is necessary to specify the `package_dir` argument.
    packages=find_packages(exclude=("tests")),
    # Specify which Python versions you support pip install' will check this
    # and refuse to install the project if the version does not match.
    python_requires='>=3.5, <4',
    #install_requires = []
    include_package_data=True,
    # If there are data files included in your packages that need to be
    # installed, specify them here.
    package_data={  # Optional
        'arkane': ['data/arkane_input_species_template.py','data/arkane_input_template.py'],
    },
    # To provide executable scripts, use entry points in preference to the
    # "scripts" keyword. Entry points provide cross-platform support and allow
    # `pip` to create the appropriate form of executable for the target
    # platform.
    #
    # For example, the following would provide a command called `sample` which
    # executes the function `main` from this package when invoked:
    entry_points={  # Optional
        'console_scripts': [
            'ccparse=compchemparser:main',
        ],
    },
)