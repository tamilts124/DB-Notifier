# DB-Notifier
This app Helps to Monitor your Program what does

# More Details

1) This Application Requires Admin Url Of Infinity Free Database

2) The Application Creates Or Use The Table From Database. Default, Name Notifier

It's Table Structure,

  create table Notifier (
        Id int auto_increment not null,
        Place varchar(100) not null,
        Level varchar(100) not null,
        NewDate varchar(100) not null,
        OldDate varchar(100) default null,
        NewTime varchar(100) not null,
        OldTime varchar(100) default null,
        Info text not null,
        Notify boolean not null default true,
        Times int not null default 1,
        primary key(Id) )

3) It Lision Every 5 minites to Checks Any Notifications are Avaible

