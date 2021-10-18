import docopt
import pyuploader.app as app

__doc__ = """pyuploader
Usage:
    ts_upload <file_or_dir>  [--url=<url>]
                             [--auth=<auth>]
                             [--file-ext=<ext>]
                             [--log-file-name=<name>]
                             [--log-file-dir=<dir>]
                             [--no-file-logging]
                             [--dry-run]

Options:
--url=<url>             Triple store upload endpoint. If not specified,
                        the code will try to read it from user
                        environment variables.
--auth=<auth>           Triple store authorization as a "username:password"
                        string. If not specified, the code will try to read
                        it from user environment variables.
--file-ext=<ext>        List of extensions used to select files       [default: owl]
                        that will be uploaded to the triple store.
--log-file-name=<name>  Name of the generated log file.               [default: ts_upload.log]
--log-file-dir=<dir>    Path to the log file storing information of
                        what has been uploaded and where. Defaults
                        to the <fileOrDir> directory.
--no-file-logging       No logging flag to a file.
--dry-run               Run the triple store uploader tool in a dry
                        run without uploading any triples.
"""

def start():
    try:
        args = docopt.docopt(__doc__)
    except docopt.DocoptExit:
        raise docopt.DocoptExit('Error: ts_upload called with wrong arguments.')

    app.ts_upload_wrapper(
        file_or_dir = args['<file_or_dir>'],
        url = args['--url'],
        auth = tuple(args['--auth'].split(':')) if args['--auth'] is not None else args['--auth'],
        file_ext = args['--file-ext'],
        log_file_dir = args['--log-file-dir'],
        log_file_name = args['--log-file-name'],
        no_file_logging = args['--no-file-logging'],
        dry_run = args['--dry-run']
    )

if __name__ == '__main__':
    start()